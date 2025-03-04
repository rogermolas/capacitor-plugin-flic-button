require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name = 'CapacitorPluginFlicButton'
  s.version = package['version']
  s.summary = package['description']
  s.license = package['license']
  s.homepage = package['repository']['url']
  s.author = package['author']
  s.source = { :git => package['repository']['url'], :tag => s.version.to_s }
  s.source_files = 'ios/Sources/**/*.{swift,h,m,c,cc,mm,cpp}'
  s.ios.deployment_target = '14.0'
  s.dependency 'Capacitor'
  s.swift_version = '5.1'

  # ✅ Use `vendored_frameworks` for `.xcframework`
  s.vendored_frameworks = 'ios/flic2lib.xcframework'

  # ✅ Required frameworks for Bluetooth functionality
  s.frameworks = 'CoreBluetooth'
  s.libraries = 'c++'
end
