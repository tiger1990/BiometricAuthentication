# BiometricAuthentication
Biometric authentication is a security process that relies on the unique biological characteristics of an individual such as Fingerprints, Facial characteristics, Voice, Eye retina.Biometric authentication is safe as compared to other authentication mechanisms available in Android like Pin, Pattern or Password because other authentication mechanisms can get compromised alike Biometric Authentication where you can only unlock the device.

Android 10 Introduces the BiometricManager class that developers can use to query the availability of biometric authentication.
Includes fingerprint and face authentication integration for BiometricPrompt

Android 9 Includes fingerprint integration only for BiometricPrompt.
Deprecates the FingerprintManager class. If your bundled and system apps use this class, update them to use BiometricPrompt and BiometricManager instead.

![alt text](https://github.com/tiger1990/BiometricAuthentication/blob/main/biometri_arch.png?raw=true)


Biometric Login With Remote Server:
Below Flow Diagram is taken from : medium post:
#https://medium.com/androiddevelopers/biometric-authentication-on-android-part-2-bc4d0dae9863

![alt text](https://github.com/tiger1990/BiometricAuthentication/biometric_state_flow.png?raw=true)
