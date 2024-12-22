# Spoken Keyword Recognition App Instructions

The project aims to improve the energy efficiency of an Android app/service which performs spoken keyword recognition. The starting app is available at the following GitHub repository for download: [https://bitbucket.org/pbdfrita/speechrecognitionapp/src/master/](https://bitbucket.org/pbdfrita/speechrecognitionapp/src/master/)

In its current form, the Android app performs continuous inference of the audio recordings from the microphone using a TensorFlow Lite neural network model trained on the Google Speech Commands v2 dataset. The app is able to recognize 35 different words, and it does not have a ‘none’ class, which means that it will always attempt to classify an input to one of the 35 classes (words), even if the input
records just silence (no speech).

Your task is to first understand the energy consumption profile of the application, and then to implement energy optimization techniques that would make this app more efficient (and implicitly improve its usability). For profiling the energy consumption you will use two approaches:

1. Software profiling ([Power profiler](https://developer.android.com/studio/profile/power-profiler))
2. Hardware profiling (again using the Monsoon Power monitor, but since smartphones do not have their power connectors/terminals exposed, you will be installing the app and performing the measurements on an embedded device running Android - [ASUS Tinkerboard](https://www.asus.com/networking-iot-servers/aiot-industrial-solutions/tinker-series/tinker-board/))

Second, you will have to implement techniques to improve energy efficiency (e.g. try to first “cheaply” detect if there is any sound information in the recording, and run inference only on non-silent samples). You have to profile your solution and show/discuss the amount of energy savings it brings. If the solution
impacts the accuracy of the inference, this trade-off should also be discussed.

Finally, the app should log its behavior (the number of predictions done each unit of time and what were the keywords predicted), and the log file should be sent periodically to a Cloud storage (e.g. [Firebase](https://firebase.google.com/docs/storage/android/upload-files)) in an energy-efficient way (more on that in the Background processing Android lecture).

Just like for the Raspberry Pi project, when presenting your solution you should also indicate possible alternatives and a comparative energy profiling study.

**Report deadline**: January 20th, 2025 (presentation on January 15th)
