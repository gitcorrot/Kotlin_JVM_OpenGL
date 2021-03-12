object ResourcesUtils {

    fun loadStringFromFile(path: String): String {
        return this.javaClass.getResource(path).readText()
    }

}