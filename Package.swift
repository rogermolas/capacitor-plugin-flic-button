// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorPluginFlicButton",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "CapacitorPluginFlicButton",
            targets: ["FlicButtonPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "FlicButtonPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/FlicButtonPlugin"),
        .testTarget(
            name: "FlickButtonPluginTests",
            dependencies: ["FlicButtonPlugin"],
            path: "ios/Tests/FlickButtonPluginTests")
    ]
)