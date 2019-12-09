import java.util.Random;

public class ZKMath {
    Random random;

    // x is a key that is known by the prover
    long x;

    // In order for this proof to be meaningful, the prover must not know
    // what prime and generator to use in advance. These should be determined
    // by the verifier so that C is uniquely determined by x in a way that
    // the prover cannot falsify.
    long p;
    long g;

    // C, r, and y are computed by the prover and are used by the verifier to
    // complete and verify the proof
    long C;
    long r;
    long y;

    // Used to determine if the verifier asked for (x + r) mod (p-1) or r
    boolean shareR;

    public ZKMath () {
        this.random = new Random(System.currentTimeMillis());
    }

    /**
     * Uses Square Mul Algorithm to compute a^d mod n
     *
     * @param a
     * @param d
     * @param n
     * @return a^d mod n
     */
    long modPow(long a, long d, long n) {
        if(d == 0) {
            return 1;
        }
        if(d % 2 == 0) {
            return modPow((a*a)%n, d/2, n);
        }
        return (a * modPow((a*a)%n, d/2, n)) % n;
    }

    /**
     * Uses Miller-Rabin primality testing to determine if a number is prime with high probability.
     * Note: this test will not invalidate Carmichael numbers as non-prime
     * There are no false negatives, but there may be false positives.
     *
     * @param n the number to test for primality
     * @param rounds the number of iterations to run Miller-Rabin testing
     * @return whether the number is likely to be a prime number
     */
    boolean millerRabin(long n, long rounds) {
        long tmp = n - 1;
        long r = 0;
        while(tmp % 2 == 0) {
            tmp /= 2;
            r++;
        }
        long d = tmp;
        //System.out.println("d" + d + ", r" + r);
        for(long i = 0; i < rounds; i++) {
            boolean pass_test = false;
            long a = random.nextInt((int) n - 3) + 2;
            long x = modPow(a, d, n);
            if(x == 1 || x == n -1) {
                continue;
            }
            for(long j = 0; j < r - 1; j++) {
                x = (x * x) % n;
                if(x == n-1) {
                    pass_test = true;
                    break;
                }
            }
            if(!pass_test) {
                //System.out.println(a); // a is the witness that shows that n is not prime.
                return false;
            }
        }
        return true;
    }

    /**
     * Generates a random prime in [2^16, 2^17).
     * The data ranges were chosen solely because longs won't have overflow issues when doing
     * modular exponentiation for integers in this range. (This is needed for Miller-Rabin.)
     *
     * The probability of finding a prime number at random in the range [2^n, 2^(n+1)) is 1/n, so
     * we need approximately n numbers before we are expected to find a prime number.
     *
     * @return a prime number in the range [2^16, 2^17)
     */
    long genProbPrime() {
        while(true) {
            long rounds = 128;
            int n = 16;
            long range = (long) Math.pow(2, n);
            long p = Math.abs(random.nextInt()) % range;
            p += range;
            if(p % 2 == 0){
                p += 1;
            }
            System.out.println(p);
            if (millerRabin(p, rounds))
                return p;
        }
    }

    /**
     * Computes the necessary iterations n such that 1 - 2^(-n) > min_threshold
     *
     * @param min_threshold the minimum threshold probability required for Zero Knowledge
     * @return the number of iterations to run
     */
    int necessaryIterations(double min_threshold) {
        double max_fail = 1 - min_threshold;
        int iters = 1;
        double prob_fail = .5;
        while (prob_fail > max_fail) {
            iters++;
            prob_fail /= 2;
        }
        return iters;
    }
}

class Main {
    public static void main(String[] args) {
        ZKMath math = new ZKMath();
        System.out.println(math.millerRabin(121, 10));
        System.out.println(math.millerRabin(97, 10));
        System.out.println(math.genProbPrime());
    }
}
