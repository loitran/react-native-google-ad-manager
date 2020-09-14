
Pod::Spec.new do |s|
  s.name         = "RNGoogleAdManager"
  s.version      = "1.0.0"
  s.summary      = "RNGoogleAdManager"
  s.description  = <<-DESC
                  RNGoogleAdManager
                   DESC
  s.homepage     = ""
  s.license      = "MIT"
  s.authors      = "Callosum Software GmbH"
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/fourtran/react-native-google-ad-manager.git", :tag => "master" }
  s.source_files  = "RNGoogleAdManager/**/*.{h,m,swift}"
  s.requires_arc = true


  s.dependency "React"
  s.dependency "Google-Mobile-Ads-SDK", "~> 7.43.0"

end

  