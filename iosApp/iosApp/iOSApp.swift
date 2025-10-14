import SwiftUI
import ComposeApp

@main
struct iOSApp: App {

    init(){
        NapierSetupKt.setupNapier()
        AppDiSetupKt.intiKoin()
        
        // Initialize app after Koin is set up
        AppInitialization.shared.initializeAppAsync()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}