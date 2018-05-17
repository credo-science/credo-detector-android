# CREDO Detector
The Android application for [CREDO project](https://credo.science/).

The Cosmic-Ray Extremely Distributed Observatory (CREDO) project is hunting for particle cascades
from deep space in an effort to answer some of
the fundamental questions about our Universe. Your particle detections will feed directly into this
pioneering new area of scientific research. You can also engage in the analysis of the detections
that you, your fellow citizen scientists and professional observatories from around the world are
making though the Dark Universe Welcome experiment.

## Build

You must download [Android SDK](https://developer.android.com/studio/index.html) and set path to it
in `ANDROID_HOME` environment variable or `local.properties`. Now you can compile from
command line:

```bash
$ ./gradlew build
```

or

```batch
gradlew.bat build
```

Now you can find compiled *apk* file in `./app/build/outputs/apk/debug`.

### Contribution

Welcome to import project to Android Studio and contribute in development. 

1. Open Android Studio.
2. `File->New->Project from Version Control->GitHub`
3. Enter the Git Repository URL and clone.

Public contribution rules is not defined yet. If you want to make changes now you can fork project
and make pull request.

#### Contribution rules for team

When you get issue to resolve you should:

1. Create new branch from master called `I{issue number}` i.e. `I5`.
2. Make modifications (new feature or fix) on this branch.
3. Call to maintainer when modifications is done.
4. Rebase branch to master.
5. Respond to the recommendations from maintainer and push to branch and call to him when finish.
6. When maintainers fully accept then merge to master.

## Acknowledgments

The CREDO Detector code is funded by: Interantional Visegrad Fund, Institute of Nuclear Physics PAN, Carcow University of Technology.
