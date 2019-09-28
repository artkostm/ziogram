# Telegram Client

## For MacOS (manual steps)

- java/scala installed
- cd to the project dir
- `xcode-select --install`
- `brew install gperf cmake openssl coreutils`
- `brew cask install`
- `cd tdlib`
- `rm -rf build`
- `mkdir build`
- `cd build`
- `cmake -DCMAKE_BUILD_TYPE=Release -DOPENSSL_ROOT_DIR=/usr/local/opt/openssl/ -DCMAKE_INSTALL_PREFIX:PATH=../example/java/td -DTD_ENABLE_JNI=ON ..`
- `cmake --build . --target install`
- `cd ..`
- `cd example/java`
- `rm -rf build`
- `mkdir build`
- `cd build`
- `cmake -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX:PATH=../../../tdlib -DTd_DIR:PATH=$(greadlink -e ../td/lib/cmake/Td) ..`
- `cmake --build . --target install`

### For Windows (not tested well)

Presets: PowerShell, bitness is 64
Download and install Microsoft Visual Studio. Enable C++ support while installing.
Download and install CMake; choose "Add CMake to the system PATH" option while installing.
Download and install Git.
Download and install gperf. Add the path to gperf.exe to the PATH environment variable.
Download and unpack PHP. Add the path to php.exe to the PATH environment variable.
Download and install JDK.
Close and re-open PowerShell if PATH environment variable was changed.
Run these commands in PowerShell to build TDLib and to install it to tdlib:

- `cd tdlib`
- `git clone https://github.com/Microsoft/vcpkg.git`
- `cd vcpkg`
- `./bootstrap-vcpkg.bat`
- `./vcpkg.exe install openssl:x64-windows zlib:x64-windows`
- `cd ..`
- `Remove-Item build -Force -Recurse -ErrorAction SilentlyContinue`
- `mkdir build`
- `cd build`
- `cmake -A x64 -DCMAKE_INSTALL_PREFIX:PATH=../example/java/td -DTD_ENABLE_JNI=ON -DCMAKE_TOOLCHAIN_FILE:FILEPATH=../vcpkg/scripts/buildsystems/vcpkg.cmake ..`
- `cmake --build . --target install --config Release`
- `cd ..`
- `cd example/java`
- `Remove-Item build -Force -Recurse -ErrorAction SilentlyContinue`
- `mkdir build`
- `cd build`
- `cmake -A x64 -DCMAKE_INSTALL_PREFIX:PATH=../../../tdlib -DCMAKE_TOOLCHAIN_FILE:FILEPATH=../../../vcpkg/scripts/buildsystems/vcpkg.cmake -DTd_DIR:PATH=$(Resolve-Path ../td/lib/cmake/Td) ..`
- `cmake --build . --target install --config Release`