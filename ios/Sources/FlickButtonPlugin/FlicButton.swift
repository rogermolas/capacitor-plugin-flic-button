import Foundation

@objc public class FlicButton: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
