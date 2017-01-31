#ifdef CONFIG_MIN
void func1() {
}
#endif

int a;

void func2() {
#ifdef CONFIG_MAX
	func1();
#endif
}

int func3() {
	return 1;
}

int func4(int param) {
	return param + func3();
}

void func5() {
	{
		int a;
		a = 
		#ifdef CONFIG_MAX
			3
		#else
			4
		#endif
		;
		func4(a);
	}
	return;
}

int main() {
#ifdef CONFIG_MIN
	{
		func2();
		func4(1);
	}
#endif
	
	return 0;
}
