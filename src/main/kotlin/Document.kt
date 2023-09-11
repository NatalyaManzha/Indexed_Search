import kotlin.math.pow
import kotlin.math.sqrt

/*Обработанный документ или запрос в поиске с массивом данных о вхождении слов из общего списка уникальных слов в его текст.
* Размер массива данных (вектора) равен размеру общего списка слов в индексированной базе.
* Метод compareData () представляет собой векторную модель сравнения двух массивов координат для запроса и документа с текстом.
* Вычисляется косинус для двух векторов - чем он больше, тем больше сходство запроса и текста документа.
*/

data class Document(
	val name : String,
	val data : Array<Float>
) {

	fun compareData(a : Array<Float>, b : Array<Float>) : Float {
		var numerator = 0.0f //числитель
		var x = 0.0f //сумма множителей для первого массива
		var y = 0.0f //сумма множителей для второго массива
		for (i in a.indices) {
			numerator += a[i] * b[i]
			x += a[i].pow(2)
			y += b[i].pow(2)
		}
		val denominator = sqrt(x) * sqrt(y) //знаменатель
		return numerator / denominator
	}
}







