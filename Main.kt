package minesweeper

import kotlin.random.Random

object MineSweeper {
    private var control = true
    private val mines: Int
    private val allHouses: MutableList<House>
    private val emptyHousesId = mutableSetOf<Int>()
    private var selectedByRandom = mutableListOf<Int>()

    object Field {
        const val row = 9
        const val column = 9
    }

    data class House(val id: Int, val row: Int, val column: Int, var state: Char = '.') {
        val nearById = when (this.id) {
            1 -> mutableListOf(2, 10, 11)
            9 -> mutableListOf(8, 17, 18)
            73 -> mutableListOf(64, 65, 74)
            81 -> mutableListOf(71, 72, 80)
            2, 3, 4, 5, 6, 7, 8 -> mutableListOf(id - 1, id + 1, id + 8, id + 9, id + 10)
            74, 75, 76, 77, 78, 79, 80 -> mutableListOf(id - 10, id - 9, id - 8, id - 1, id + 1)
            10, 19, 28, 37, 46, 55, 64 -> mutableListOf(id - 9, id - 8, id + 1, id + 9, id + 10)
            18, 27, 36, 45, 54, 63, 72 -> mutableListOf(id - 10, id - 9, id - 1, id + 8, id + 9)
            else -> mutableListOf(id - 10, id - 9, id - 8, id - 1, id + 1, id + 8, id + 9, id + 10)
        }
        var minesNearBy = 0
    }

    private val createAllHouses = {
        val houses = mutableListOf<House>()
        var id = 1
        for (row in 1..Field.row) {
            for (column in 1..Field.column) {
                houses.add(House(id, row, column, '.'))
                id++
            }
        }
        houses
    }

    private fun transform(column: Int, row: Int): Int = row * Field.column - (Field.column - column)

    private fun selectRandomNumbers(column: String, row: String): MutableList<Int> {
        val selected = mutableListOf<Int>()
        val forbiddenIds = mutableListOf<Int>()
        val forbiddenId = transform(column.toInt(), row.toInt())
        forbiddenIds.add(forbiddenId)
        for (id in findHouse(forbiddenId).nearById) {
            forbiddenIds.add(id)
        }
        for (mine in 1..mines) {
            while (true) {
                val randomId = Random.nextInt(1, (Field.row * Field.column) + 1)
                if (randomId !in selected && randomId !in forbiddenIds) {
                    selected.add(randomId)
                    break
                }
            }
        }
        return selected
    }

    private fun findHouse(row: Int, column: Int): House {
        for (house in allHouses) {
            if (house.row == row && house.column == column) return house
        }
        return House(-1, -1, -1, 'E')
    }

    private fun findHouse(id: Int): House {
        for (house in allHouses) {
            if (house.id == id) return house
        }
        return House(-1, -1, -1, 'E')
    }

    private val printField = {
        println(" │123456789│\n—│—————————│")
        for (row in 1..Field.row) {
            print("$row|")
            for (column in 1..Field.column) {
                print(findHouse(row, column).state)
            }
            println("|")
        }
        println("—│—————————│")
    }

    private fun verifyMinesNearBy() {
        for (house in allHouses) {
            var count = 0
            for (id in house.nearById) {
                if (id in selectedByRandom) {
                    count++
                }
            }
            house.minesNearBy = count
        }
    }

    private fun verify(): Boolean {
        var asterisks = 0
        for (house in allHouses) {
            if (house.state == '*') asterisks++
        }
        for (selected in selectedByRandom) {
            if (findHouse(selected).state != '*') {
                return false
            }
        }
        if (asterisks == mines) return true
        return false
    }

    private fun verify2(): Boolean {
        var dots = 0
        for (house in allHouses) {
            if (house.state == '.') dots++
        }
        for (selected in selectedByRandom) {
            if (findHouse(selected).state != '.') {
                return false
            }
        }
        if (dots == mines) return true
        return false
    }

    private val askUser = {
        print("\nSet/unset mines marks or claim a cell as free: ")
        readLine()!!.split(" ")
    }

    private val isIdZero = { id: Int -> findHouse(id).minesNearBy == 0 }

    private fun mapEmptyHousesId(house: House) {
        emptyHousesId.add(house.id)
        for (id in house.nearById) {
            if (isIdZero(id)) emptyHousesId.add(id)
        }
        while (true) {
            val housesId = mutableListOf<Int>()
            housesId.addAll(emptyHousesId)
            for (id in housesId) {
                for (id in findHouse(id).nearById) {
                    if (id !in emptyHousesId && isIdZero(id)) emptyHousesId.add(id)
                }
            }
            if (housesId.size == emptyHousesId.size) break
        }
    }

    private fun updateState() {
        for (id in emptyHousesId) {
            findHouse(id).state = '/'
            for (idNumber in findHouse(id).nearById) {
                if (findHouse(idNumber).minesNearBy > 0) {
                    findHouse(idNumber).state = findHouse(idNumber).minesNearBy.digitToChar()
                }
            }
        }
    }

    init {
        print("How many mines do you want on the field? ")
        mines = readLine()!!.toInt()
        allHouses = createAllHouses()
        printField()
    }

    fun playGame() {
        while (true) {
            val (column, row, string) = askUser()
            if (string == "free" && control) {
                selectedByRandom = selectRandomNumbers(column, row)
                //println("selectedByRandom: ${selectedByRandom.sorted()}")
                verifyMinesNearBy()
                control = false
            }
            val house = findHouse(row.toInt(), column.toInt())
            when (string) {
                "mine" -> {
                    house.state = when (house.state) {
                        '*' -> '.'
                        '.' -> '*'
                        else -> 'E' // Error
                    }
                }
                "free" -> {
                    if (house.id in selectedByRandom) {
                        for (house in allHouses) {
                            if (house.id in selectedByRandom) {
                                house.state = 'X'
                            }
                        }
                        printField()
                        println("You stepped on a mine and failed!")
                        break
                    } else if (house.minesNearBy == 0) {
                        mapEmptyHousesId(house)
                        updateState()
                    } else {
                        house.state = house.minesNearBy.digitToChar()
                    }
                }
            }
            printField()
            if (verify() || verify2()) {
                println("Congratulations! You found all the mines!")
                break
            }
        }
    }
}


fun main() {
    MineSweeper.playGame()
}
