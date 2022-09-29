print_red() {
  printf "\e[1;31m$1\e[0m"
}
print_green() {
  printf "\e[1;32m$1\e[0m"
}
print_yellow() {
  printf "\e[1;33m$1\e[0m"
}
print_blue() {
  printf "\e[1;34m$1\e[0m"
}

print_blue "\nStarting...\n\n"

print_blue "cd into working directory...\n\n"
cd "/Users/khuram/Projects/done/Notes"

print_blue "run unit tests...\n"
./gradlew test
print_green "\nunit tests COMPLETE.\n\n"

print_blue "run androidTests...\n"
./gradlew connectedAndroidTest
print_green "\nandroidTests COMPLETE."

print_yellow "\n\nAll tests complete...\n\n"