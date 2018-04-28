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
		command = command + " 127.0.0.1 ";
		command = command + " " + to_string((3000 + stoi(argv[1])));
		command = command + " 2999 3000";
		system(command.c_str());
        exit(0);
    }
	return 0;
}