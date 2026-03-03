package com.android.one

data class Weapon(val name: String, val power: Int)

enum class CharacterType(val baseHp: Int, val weapon: Weapon) {
    WARRIOR(baseHp = 120, weapon = Weapon("Sword", 25)),
    MAGUS(baseHp = 140, weapon = Weapon("Staff", 16)),
    COLOSSUS(baseHp = 170, weapon = Weapon("Hammer", 22)),
    DWARF(baseHp = 90, weapon = Weapon("Axe", 34));

    companion object {
        fun fromChoice(choice: Int): CharacterType? = when (choice) {
            1 -> WARRIOR
            2 -> MAGUS
            3 -> COLOSSUS
            4 -> DWARF
            else -> null
        }
    }
}

interface Attacker {
    fun attack(target: Character): Int
}

interface Healer {
    fun heal(target: Character): Int
}

abstract class Character(
    val type: CharacterType,
    val name: String,
    private val maxHp: Int,
    val weapon: Weapon
) : Attacker {
    private var hp: Int = maxHp

    val isAlive: Boolean
        get() = hp > 0

    val currentHp: Int
        get() = hp

    fun takeDamage(amount: Int): Int {
        if (!isAlive) return 0
        val actualDamage = amount.coerceAtLeast(0).coerceAtMost(hp)
        hp -= actualDamage
        return actualDamage
    }

    fun receiveHealing(amount: Int): Int {
        if (!isAlive) return 0
        val healed = amount.coerceAtLeast(0).coerceAtMost(maxHp - hp)
        hp += healed
        return healed
    }

    override fun attack(target: Character): Int {
        if (!isAlive) return 0
        return target.takeDamage(weapon.power)
    }

    abstract fun action(): String
}

class Warrior(name: String) : Character(
    type = CharacterType.WARRIOR,
    name = name,
    maxHp = CharacterType.WARRIOR.baseHp,
    weapon = CharacterType.WARRIOR.weapon
) {
    override fun action(): String = "Strike"
}

class Magus(name: String) : Character(
    type = CharacterType.MAGUS,
    name = name,
    maxHp = CharacterType.MAGUS.baseHp,
    weapon = CharacterType.MAGUS.weapon
), Healer {
    override fun heal(target: Character): Int {
        val healingPower = weapon.power + 12
        return target.receiveHealing(healingPower)
    }

    override fun action(): String = "Attack or Heal"
}

class Colossus(name: String) : Character(
    type = CharacterType.COLOSSUS,
    name = name,
    maxHp = CharacterType.COLOSSUS.baseHp,
    weapon = CharacterType.COLOSSUS.weapon
) {
    override fun action(): String = "Crush"
}

class Dwarf(name: String) : Character(
    type = CharacterType.DWARF,
    name = name,
    maxHp = CharacterType.DWARF.baseHp,
    weapon = CharacterType.DWARF.weapon
) {
    override fun action(): String = "Heavy Strike"
}

data class Player(val name: String, val team: MutableList<Character>) {
    fun livingCharacters(): List<Character> = team.filter { it.isAlive }
    fun isDefeated(): Boolean = livingCharacters().isEmpty()
}

class BattleArenaGame {
    private val usedNames = mutableSetOf<String>()
    private val players = mutableListOf<Player>()
    private var turnCount = 0

    fun run() {
        println("=== Battle Arena ===")
        setupPlayers()
        println("\nTeams are ready. Let the battle begin!\n")
        battleLoop()
        printSummary()
    }

    private fun setupPlayers() {
        for (index in 1..2) {
            val playerName = promptNonEmpty("Enter Player $index name:")
            val team = createTeam(playerName)
            players += Player(playerName, team)
        }
    }

    private fun createTeam(playerName: String): MutableList<Character> {
        val selectedTypes = mutableSetOf<CharacterType>()
        val team = mutableListOf<Character>()
        println("\n$playerName, create your team (3 characters):")

        while (team.size < 3) {
            println("\nCharacter ${team.size + 1}/3")
            showAvailableTypes(selectedTypes)
            val type = chooseType(selectedTypes)
            val characterName = chooseUniqueName("Choose a unique character name:")
            team += buildCharacter(type, characterName)
            selectedTypes += type
        }

        return team
    }

    private fun showAvailableTypes(selectedTypes: Set<CharacterType>) {
        fun availability(type: CharacterType): String = if (selectedTypes.contains(type)) "(already used)" else ""
        println("1. Warrior  HP:${CharacterType.WARRIOR.baseHp}  ATK:${CharacterType.WARRIOR.weapon.power} ${availability(CharacterType.WARRIOR)}")
        println("2. Magus    HP:${CharacterType.MAGUS.baseHp}  ATK:${CharacterType.MAGUS.weapon.power} + heal ${availability(CharacterType.MAGUS)}")
        println("3. Colossus HP:${CharacterType.COLOSSUS.baseHp}  ATK:${CharacterType.COLOSSUS.weapon.power} ${availability(CharacterType.COLOSSUS)}")
        println("4. Dwarf    HP:${CharacterType.DWARF.baseHp}  ATK:${CharacterType.DWARF.weapon.power} ${availability(CharacterType.DWARF)}")
    }

    private fun chooseType(selectedTypes: Set<CharacterType>): CharacterType {
        while (true) {
            val choice = promptInt("Choose type (1-4):")
            val type = CharacterType.fromChoice(choice)
            if (type == null) {
                println("Invalid choice. Try again.")
                continue
            }
            if (selectedTypes.contains(type)) {
                println("This type is already used in this team. Choose another one.")
                continue
            }
            return type
        }
    }

    private fun chooseUniqueName(message: String): String {
        while (true) {
            val name = promptNonEmpty(message)
            if (usedNames.add(name.lowercase())) return name
            println("This name is already taken in this game. Try another name.")
        }
    }

    private fun buildCharacter(type: CharacterType, name: String): Character = when (type) {
        CharacterType.WARRIOR -> Warrior(name)
        CharacterType.MAGUS -> Magus(name)
        CharacterType.COLOSSUS -> Colossus(name)
        CharacterType.DWARF -> Dwarf(name)
    }

    private fun battleLoop() {
        var activePlayerIndex = 0

        while (!players[0].isDefeated() && !players[1].isDefeated()) {
            val active = players[activePlayerIndex]
            val opponent = players[1 - activePlayerIndex]
            turnCount++

            println("\n--- Turn $turnCount ---")
            println("${active.name}'s turn")

            val actor = chooseCharacter("Choose your character:", active.livingCharacters())
            if (actor is Healer) {
                val actionChoice = promptInt("Choose action: 1) Attack  2) Heal")
                if (actionChoice == 2) {
                    val target = chooseCharacter("Choose an ally to heal:", active.livingCharacters())
                    val healed = actor.heal(target)
                    println("${actor.name} heals ${target.name} for $healed HP. ${target.name} now has ${target.currentHp} HP.")
                } else {
                    performAttack(actor, opponent)
                }
            } else {
                performAttack(actor, opponent)
            }

            activePlayerIndex = 1 - activePlayerIndex
        }
    }

    private fun performAttack(attacker: Character, opponent: Player) {
        val target = chooseCharacter("Choose an enemy to attack:", opponent.livingCharacters())
        val damage = attacker.attack(target)
        println("${attacker.name} (${attacker.type.name}) uses ${attacker.action()} and deals $damage damage to ${target.name}.")
        if (!target.isAlive) {
            println("${target.name} is dead.")
        } else {
            println("${target.name} has ${target.currentHp} HP left.")
        }
    }

    private fun chooseCharacter(message: String, options: List<Character>): Character {
        while (true) {
            println(message)
            options.forEachIndexed { index, character ->
                println("${index + 1}. ${character.name} (${character.type.name}) - HP ${character.currentHp}")
            }
            val choice = promptInt("Enter choice (1-${options.size}):")
            if (choice in 1..options.size) return options[choice - 1]
            println("Invalid choice. Try again.")
        }
    }

    private fun printSummary() {
        val winner = players.firstOrNull { !it.isDefeated() }
        println("\n=== Game Over ===")
        println("Winner: ${winner?.name ?: "No winner"}")
        println("Turns played: $turnCount")
        println("\nFinal status:")
        players.forEach { player ->
            println("\n${player.name}")
            player.team.forEach { character ->
                val status = if (character.isAlive) "ALIVE" else "DEAD"
                println("- ${character.name} | ${character.type.name} | HP ${character.currentHp} | ${character.weapon.name}(${character.weapon.power}) | $status")
            }
        }
    }

    private fun promptNonEmpty(message: String): String {
        while (true) {
            println(message)
            val value = readLine()?.trim().orEmpty()
            if (value.isNotEmpty()) return value
            println("Input cannot be empty.")
        }
    }

    private fun promptInt(message: String): Int {
        while (true) {
            println(message)
            val number = readLine()?.trim()?.toIntOrNull()
            if (number != null) return number
            println("Please enter a valid number.")
        }
    }
}

fun main() {
    BattleArenaGame().run()
}


