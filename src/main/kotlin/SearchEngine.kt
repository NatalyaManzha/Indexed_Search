import java.io.File
import kotlin.system.exitProcess

/*Синглтон Поисковик объединяет в себе весь механизм построения индексированной базы и осуществления поиска по запросу.
* Содержит в себе следующие поля:
* sortedDictionery - общий отсортированный список уникальных слов во всех текстах для поиска;
* textPrimaryData - промежуточный этап хранения данных о текстах с частотными словарями;
* Docs - итоговый список Документов с их "векторами".*/

object SearchEngine {

	private var sortedDictionery : List<String> = mutableListOf()
	private var textPrimaryData : MutableMap<String, MutableMap<String, Float>> = mutableMapOf()
	private var Docs = mutableListOf<Document>()

	/*Метод расчета для текста словаря с указанным удельным весом (УВ) в тексте каждого слова и составление общего словаря.
	* Переменные:
	* dictionary - 	общий список уникальных слов во всех текстах для поиска;
	* frequencyTable - частотный словарь для конкретного текста;
	* tableOfSpecificFrequencies - словарь с УВ слова.
	* Полученные данные сохраняются на промежуточном этапе в textPrimaryData в виде карты "Название файла" - "Словарь с УВ".
	* Также получаем отсортированный в алфавитном порядке общий словарь в sortedDictionery*/

	private fun collectPrimaryData(path : String) {
		val dictionary = mutableSetOf<String>()
		File(path).listFiles()!!.forEach { file ->
			val frequencyTable = mutableMapOf<String, Int>()
			val tableOfSpecificFrequencies = mutableMapOf<String, Float>()
			val listOfWords = createListOfWords(file.readText())
			val numberOfWordsInText = listOfWords.size
			listOfWords.forEach { word ->
				dictionary.add(word)
				val count = frequencyTable[word] ?: 0
				frequencyTable[word] = count + 1
			}
			frequencyTable.forEach { (word, numberOfAppearances) ->
				tableOfSpecificFrequencies[word] = numberOfAppearances.toFloat() / numberOfWordsInText
			}
			textPrimaryData[file.name] = tableOfSpecificFrequencies
		}
		sortedDictionery = dictionary.sorted().toList()
	}

	/*Метод построения индексированной базы данных.
	* На основании первичных данных о каждом тексте из textPrimaryData формируем для него массив с данными о вхождении
	* в него каждого слова из общего словаря (вектор).
	* Для слов общего списка, используемых в конкретном тексте, назначается их УВ, остальным присваивается 0.0.
	* Создается объект Документ (его имя, вектор) и добавляется в список документов базы данных для поиска.*/

	fun createIndexedDataBase(path : String) {
		collectPrimaryData(path)
		textPrimaryData.forEach { (fileName, tableOfSpecificFrequencies) ->
			val data = Array<Float>(sortedDictionery.size) { 0.0f }
			sortedDictionery.forEachIndexed { index, word ->
				if (word in tableOfSpecificFrequencies.keys) data[index] = tableOfSpecificFrequencies[word]!!
			}
			Docs.add(Document(fileName, data))
		}
	}

	/*Метод преобразования текста в список слов:
	* - согласно указанному паттерну убираем небуквенные символы, заменяя их пробелами;
	* - переводим весь текст в один регистр;
	* - текст "режем" на слова, разделенные пробелами. */

	private fun createListOfWords(text : String) = run {
		Regex("[^\\p{L}0-9']+").replace(text.trim(), " ").lowercase().split(" ")
	}

	/*Метод обработки поискового запроса.
	* - аналогично с текстом переводим его в список слов;
	* - формируем данные о вхождении в запрос слов из общего словаря (вектор);
	* - результат сохраняем в объекте типа Документ.*/

	private fun createRequest(textIn : String) : Document {
		val listOfWords = createListOfWords(textIn)
		val data = Array<Float>(sortedDictionery.size) { 0.0f }
		sortedDictionery.forEachIndexed { index, word ->
			if (word in listOfWords.toSet()) data[index] = 1.0f
		}
		return Document(textIn, data)
	}

	/*Метод поиска. Получает введенную пользователем строку для поиска, выводит в консоль результирующий список файлов
	с показателем релевантности запросу поиска:
	* - инициирует обработку запроса;
	* - если сумма координат вектора запроса = 0, сообщаем о том, что ничего не найдено;
	* - каждый документ сравнивается с запросом через сопоставление их векторов через Косинусное сходство;
	* - формирует список "Файл" - "Коэффициент релевантности файла запросу";
	* - вызывает метод вывода результата. */

	fun search(textIn : String) {
		val request = createRequest(textIn)
		if (request.data.sum() == 0.0f) {
			println("К сожалению, поиск не дал результатов")
			exitProcess(0)
		} else {
			val result = mutableMapOf<String, Float>() //
			Docs.forEach { document ->
				val cos = document.compareData(request.data, document.data)
				result[document.name] = cos //получаем
			}
			showResult(request, result)
		}
	}

	/*Метод вывода результата поиска*/

	private fun showResult(request : Document, result : MutableMap<String, Float>) {
		println("По запросу '${request.name}' получен следующий ответ:")
		result.toList().sortedByDescending { it.second }
			.forEach { nameAndData ->
				if (nameAndData.second > 0) println("В файле '${nameAndData.first}' - показатель релевантности ${nameAndData.second}  ")
			}
	}
}

