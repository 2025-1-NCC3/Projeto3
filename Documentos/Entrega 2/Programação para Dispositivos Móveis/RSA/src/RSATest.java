
import java.math.BigInteger;
import java.security.SecureRandom; //gerador de numeros aleatorios criptograficamente seguros

public class RSATest {
     public static void main (String args[]) {
    	 String chave = "mPhoRFh1cxOXROy/70dgNzPBSTw=";
    	 String chaveCifrada = null;
    	 String chaveDecifrada = null;
    	 BigInteger n, d, e;
    	 int bitlen = 2048; //definindo o a length do bit
    	 
    	 //escolher a forma aleatoria dos dois numeros primos grandes
    	 SecureRandom r = new SecureRandom();
    	 BigInteger p = new BigInteger(bitlen /2, 100, r);
    	 BigInteger q = new BigInteger(bitlen /2, 100, r);
    	 
    	 //compute n = p * q 
    	 // n = nodulo
    	 n = p.multiply(q);
    	 
    	 //compute a funcao de totiente phi(n) = (p -1) (q -1)
    	 //m = valor da função totiente de n
    	 BigInteger m = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
    	 
    	 //Escolher um numero inteiro que sejam primos entre si
    	 // e = chave publica
    	 e = new BigInteger("3"); // 1<"e"<phi(n)
    	 while (m.gcd(e).intValue()>1) e = e.add(new BigInteger("2"));
    	 
    	 //d seja multiplicativo de e
    	 //calculo da chave privada
    	 d = e.modInverse(m);
    	 
    /*	 System.out.println("p: "+p);
    	 System.out.println("q: "+q);
    	 System.out.println("n: "+n);
    	 System.out.println("e: "+e);
    	 System.out.println("d: "+d);
    	 */
    	 //mensagem cifrada
    	 chaveCifrada = new BigInteger(chave.getBytes()).modPow(e, n).toString();
    	 System.out.println("Chave cifrada: "+chaveCifrada);
    	 
    	 //chave decifrada
    	 chaveDecifrada = new String(new BigInteger(chaveCifrada).modPow(d, n).toByteArray());
    	 System.out.println("Chave decifrada: "+chaveDecifrada);
     }    
}
