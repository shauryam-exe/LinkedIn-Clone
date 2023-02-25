package com.codewithshadow.linkedin_clone.data.model

sealed class State {
    object Success: State()
    class Failure(e: Exception): State()
    object Loading: State()
}
