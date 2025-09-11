import SwiftUI
import ComposeApp

@main
struct iOSApp: App {

    init(){
        AppDiSetupKt.intiKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}