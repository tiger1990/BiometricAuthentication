# BiometricAuthentication
Biometric authentication is a security process that relies on the unique biological characteristics of an individual such as Fingerprints, Facial characteristics, Voice, Eye retina.Biometric authentication is safe as compared to other authentication mechanisms available in Android like Pin, Pattern or Password because other authentication mechanisms can get compromised alike Biometric Authentication where you can only unlock the device.

Android 10 Introduces the BiometricManager class that developers can use to query the availability of biometric authentication.
Includes fingerprint and face authentication integration for BiometricPrompt

Android 9 Includes fingerprint integration only for BiometricPrompt.
Deprecates the FingerprintManager class. If your bundled and system apps use this class, update them to use BiometricPrompt and BiometricManager instead.

![image](https://github.com/tiger1990/BiometricAuthentication/blob/main/biometri_arch.png?raw=false)


BiometricPrompt class is used to prompt a system dialog to the user requesting to authenticate using biometric credentials. Let’s initialize BiometricPrompt class first, it has 3 parameter
Context
Executor: Allows you to specify a thread on which your callbacks should be run
AuthenticationCallback: An abstract class which have 3 methods onAuthenticationError(Called when an unrecoverable error has been encountered and the operation is complete)
onAuthenticationSucceeded(Called when a biometric is recognized)
onAuthenticationFailed(Called when a biometric is valid but not recognized)



Biometric Login With Remote Server:
Below Flow Diagram is taken from : medium post:
#https://medium.com/androiddevelopers/biometric-authentication-on-android-part-2-bc4d0dae9863

![alt text](https://github.com/tiger1990/BiometricAuthentication/blob/main/biometric_state_flow.png?raw=true)



#Biometrics + Cryptography

Crypto-based authentication is about using -biometric- authentication to perform an encryption or decryption operation. 
It secures the use of a cryptographic secret key by ensuring it’s only ever accessed if the user is successfully recognized -authenticated-.
Using crypto-based authentication is supported from API level 23, and assumes the use of a strong authenticator starting from API level 30. 

It requires the use of a CryptoObject, which is a wrapper around a Cipher, MAC, or Signature.
You can perform a crypto-based authentication by generating a secret key and storing in the keyStore,
creating and configuring a cryptographic operation, like a Cipher, wrapping it in aCryptoObject then passing it to a BiometricPrompt when authenticating.


Crypto-based authentication can replace remote server authentication. Instead of requesting your server to authenticate a user every time they open your app, you can instead require a remote server authentication the very first time they use the app, or whenever their token expires and is no longer valid. Once authenticated by the server, the app can encrypt the server token using a secret key backed by the user’s biometrics, then store the encrypted token on disk.

Later when the user opens the app again, they will be authenticated by decrypting the stored -encrypted- token using their biometrics.


