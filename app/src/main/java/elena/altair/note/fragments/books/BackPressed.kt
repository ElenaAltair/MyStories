package elena.altair.note.fragments.books

// интерфейс необходим для обработки событий нажатие
// кнопки back или home,
// когда запущен тот или иной фрагмент,
// чтобы связать функции в активити с функциями во фрагментах
interface BackPressed {
    fun handleOnBackPressed()
    fun onDestroy()
}