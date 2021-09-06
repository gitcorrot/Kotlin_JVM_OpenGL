import org.koin.dsl.module

val cameraModule = module {
    single { Camera() }
}

val worldModule = module {
    single { World() }
}

val inputManagerModule = module {
    single { InputManager() }
}