import android.content.Context
import android.content.SharedPreferences
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.IOException

class InternalStorageHelper (private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    // Папки для хранения музыки и фото во внутреннем хранилище
    private val musicDir: File = File(context.filesDir, "music")
    private val photoDir: File = File(context.filesDir, "photos")

    init {
        // Создаем папки, если они не существуют
        if (!musicDir.exists()) {
            musicDir.mkdirs()
        }
        if (!photoDir.exists()) {
            photoDir.mkdirs()
        }
    }

    // Метод для копирования файлов единожды
    fun copyFilesOnce() {
        if (!sharedPreferences.getBoolean("files_copied", false)) { // Если файлы еще не скопированы
            copyMusicFiles()
            copyPhotoFiles()
            sharedPreferences.edit().putBoolean("files_copied", true).apply() // Устанавливаем флаг "файлы скопированы"
        }
    }

    // Копирование музыкальных файлов из assets в внутреннее хранилище
    fun copyMusicFiles() {
        //Список скачанных песен
        val musicFiles = context.assets.list("music")?.toList() ?: emptyList()
        for (fileName in musicFiles) {
            val assetFile = context.assets.open("music/$fileName")
            val outputFile = File(musicDir, fileName)
            copyFile(assetFile, outputFile)
        }
    }

    // Копирование фотографий из assets в внутреннее хранилище
    fun copyPhotoFiles() {
        val photoFiles = context.assets.list("photos")?.toList() ?: emptyList()// Список файлов в assets
        for (fileName in photoFiles) {
            val assetFile = context.assets.open("photos/$fileName")
            val outputFile = File(photoDir, fileName)
            copyFile(assetFile, outputFile)
        }
    }

    // Метод для копирования файла
    private fun copyFile(inputStream: InputStream, outputFile: File) {
        try {
            val outputStream = FileOutputStream(outputFile)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Метод для проверки, были ли уже записаны файлы
    fun areFilesCopied(): Boolean {
        // Получаем список всех файлов в папке "music" из assets
        val musicFiles = context.assets.list("music")?.toList() ?: emptyList()

        // Получаем список всех файлов в папке "photos" из assets
        val photoFiles = context.assets.list("photos")?.toList() ?: emptyList()

        for (fileName in musicFiles) {
            if (!File(musicDir, fileName).exists()) return false
        }
        for (fileName in photoFiles) {
            if (!File(photoDir, fileName).exists()) return false
        }
        return true
    }
}