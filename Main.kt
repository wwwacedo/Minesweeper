package minesweeper

import kotlin.random.Random

object MineSweeper {
    object Field {
        const val row = 9
        const val column = 9
    }

    data class House(val id: Int, val row: Int, val column: Int, var state: Char = '.') {
        val nearById = when(this.id) {
            1 -> mutableListOf(2, 10, 11)
            9 -> mutableListOf(8, 17, 18)
            73 -> mutableListOf(64, 65, 74)
            81 -> mutableListOf(71, 72, 80)
            2, 3, 4, 5, 6, 7, 8 -> mutableListOf(id - 1, id + 1, id + 8, id + 9, id + 10)
            74, 75, 76, 77, 78, 79 -> mutableListOf(id - 10, id - 9, id - 8, id - 1, id + 1)
            10, 19, 28, 37, 46, 55, 64 -> mutableListOf(id - 9, id - 8, id + 1, id + 9, id + 10)
            18, 27, 36, 45, 54, 63, 72 -> mutableListOf(id - 10, id - 9, id - 1, id + 8, id + 9)
            else -> mutableListOf(id - 10, id - 9, id - 8, id - 1, id + 1, id + 8, id + 9, id + 10)
        }
        var minesNearBy = 0
    }

    private val allHouses: MutableList<House>

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

    private val selectedByRandom: MutableList<Int>

    private val selectNumbers = {
        val selected = mutableListOf<Int>()
        for (mine in 1..mines) {
            while (true) {
                val random = Random.nextInt(1, (Field.row * Field.column) + 1)
                if (random !in selected) {
                    selected.add(random)
                    break
                }
            }
        }
        selected
    }

    private val mines: Int

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

    /*
        private fun putMine() {
            for (select in selectedByRandom) {
                findHouse(select).marker = 'X'
            }
        }
    */
    private val printField = {
        println(" │123456789│\n—│—————————│")
        for (row in 1..Field.row) {
            print("$row|")
            for (column in 1..Field.column) {
                val house = findHouse(row, column)
                print(
                    if (house.minesNearBy > 0) house.minesNearBy
                    else house.state
                )
            }
            println("|")
        }
        println("—│—————————│")
    }

    private fun verifyMinesNearBy(){
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

    private fun verify(): Boolean{
        for (selected in selectedByRandom) {
            if (findHouse(selected).state != '*'){
                return false
            }
        }
        return true
    }

    init {
        print("How many mines do you want on the field? ")
        mines = readLine()!!.toInt()
        allHouses = createAllHouses()
        selectedByRandom = selectNumbers()
        verifyMinesNearBy()
        printField()
    }

    fun playGame() {
        while (true) {
            print("\nSet/delete mines marks (x and y coordinates):")
            val (x, y) = readLine()!!.split(" ").map { it.toInt() }
            val house = findHouse(y, x)
            when (house.state) {
                '.' -> house.state = '*'
                '*' -> house.state = '.'
                else -> {
                    println("There is a number here!")
                }
            }
            printField()
            if (verify()) {
                println("Congratulations! You found all the mines!")
                break
            }
        }
    }
}

fun main() {
    MineSweeper.playGame()
}
