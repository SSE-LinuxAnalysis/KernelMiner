int func(int a, int b) {
#ifdef CONFIG_MAX
    if (a < b) {
#else
    if (a > b) {
#endif    
        return b;
    } else {
        return a;
    }
}
