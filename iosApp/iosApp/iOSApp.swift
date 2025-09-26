import SwiftUI
import ComposeApp

@main
struct iOSApp: App {

    init(){
        NapierSetupKt.setupNapier()
        AppDiSetupKt.intiKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}