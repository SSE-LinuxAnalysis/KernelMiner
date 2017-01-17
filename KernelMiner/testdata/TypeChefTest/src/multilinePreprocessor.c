
int func(int a, int b) {
#if (defined(CONFIG_MIN) && \
		defined(CONFIG_MAX))
	return a;
#else
	return b;
#endif	
}


int main() {
	return func(7, 4);
}
