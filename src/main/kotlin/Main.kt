import SearchEngine.createIndexedDataBase
import SearchEngine.search

fun main() {
	val path = "C:\\Users\\User\\IdeaProjects\\Indexed_Search\\Texts"
	createIndexedDataBase(path)
	println("Введите запрос")
	val textIn = readln()
	search(textIn)
}
