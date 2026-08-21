// Example 4: Fibonacci Sequence Generator
int count = 10;
int t1 = 0;
int t2 = 1;
int nextTerm;
int step = 1;

while (step <= count) {
    print(t1);
    nextTerm = t1 + t2;
    t1 = t2;
    t2 = nextTerm;
    step = step + 1;
}
