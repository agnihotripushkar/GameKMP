# kmp
Kotlin-Multiplatform with compose- multiplatform

## Libraries Used
1. Ktor 
2. Koin (DI)
3. sqldelight (DB)
4. navigation-compose
5. coil

## Features

### 🎮 Core Game Management
- **Game Discovery**: Browse and search games with detailed information
- **Game Details**: View comprehensive game information including ratings, descriptions, and media
- **Cross-Platform Support**: Runs on Android and iOS with shared business logic

### 📚 Game Collections
- **Default Collections**: Automatic creation of Wishlist, Currently Playing, and Completed collections
- **Custom Collections**: Create personalized collections with custom names and descriptions
- **Game Organization**: Add and remove games from collections with easy management
- **Collection Statistics**: View game counts and collection overview
- **Collection Operations**: Edit, delete, and manage custom collections (default collections are protected)
- **Performance Optimized**: Fast loading with caching and optimistic updates

### ⭐ Personal Ratings & Reviews
- **Personal Rating System**: Rate games from 1-5 stars separate from aggregate ratings
- **Personal Reviews**: Write detailed reviews (up to 1000 characters) for your games
- **Quick Rating**: Fast rating directly from collection views without opening full details
- **Rating Statistics**: View your rating patterns, averages, and review counts
- **Review Management**: Edit and delete your personal reviews
- **Local Storage**: All ratings and reviews are stored locally for privacy

### 🔧 Technical Features
- **Compose UI Stability**: Fixed scrolling component crashes and infinite constraint issues
- **Proper Layout Hierarchy**: Optimized nested scrollable components for smooth performance
- **Error Handling**: Comprehensive error handling with user-friendly messages
- **Data Persistence**: Reliable local database storage with SQLDelight
- **Responsive Design**: Optimized performance with proper constraint handling

### 🎯 User Experience
- **Intuitive Navigation**: Smooth navigation between screens with proper transitions
- **Loading States**: Appropriate loading indicators and empty states
- **Pull-to-Refresh**: Refresh functionality across collection screens
- **Validation**: Input validation with clear error messages
- **Offline Support**: Full functionality without internet connection for personal data

## Screenshots

### HomeScreen
![Screenshot_20250925_173000_GameKMP](https://github.com/user-attachments/assets/5ff002df-c846-4e41-9433-3be257ac600e)

### Details Screen
![Screenshot_20250925_173148_GameKMP](https://github.com/user-attachments/assets/07c102df-9ab2-4e3e-8775-1db783fac2d5)

## Architecture
- **Kotlin Multiplatform**: Shared business logic across platforms
- **Compose Multiplatform**: Modern declarative UI framework
- **Clean Architecture**: Separation of concerns with proper layering
- **Dependency Injection**: Koin for dependency management
- **Local Database**: SQLDelight for type-safe database operations
- **Networking**: Ktor client for API communications
