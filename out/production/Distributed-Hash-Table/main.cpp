#include <iostream>
#include <cmath>
#include <cstdlib>
#include <string>
#include <unistd.h>

using namespace std;

int main(int argc, char** argv) {
   if(fork() == 0){ 
	 	string command("java main slave ");
		command = command + argv[1];
		system(command.c_str());
        exit(0);
    }
	return 0;
}