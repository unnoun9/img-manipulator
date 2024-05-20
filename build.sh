# chmod +x build.sh
# ./build.sh
mkdir -p bin
javac -d bin -cp "lib/*;src/" src/*.java 
java -Djava.library.path="lib" -cp "lib/*;bin/" Main