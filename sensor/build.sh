#!/bin/sh
mkdir buildtempfolder2
cd buildtempfolder2
cmake -G Ninja -DMOVESENSE_CORE_LIBRARY=../MovesenseCoreLib/ -DCMAKE_TOOLCHAIN_FILE=../MovesenseCoreLib/toolchain/gcc-nrf52.cmake ../accelerometer_app

ninja dfupkg
cp movesense_dfu.zip ../
cd ..

