package inet.data;

public class Parameters {	
	
	// Initial parametrization
	private static double a0; // initial productivity of the two sectors
	private static double w0; // initial wage
	private static double nw10; // net wealth of k-firms at t = 0
	private static double nw20; // net wealth of c-firms at t = 0
	private static double k0; // initial stock of capital of c-firms
	private static double ls; // labour supply = number of households = number of consumer
	
	// K-FIRMS
	private static double mu1; // fixed mark-up, price equation
	private static double nu; // fraction of past sales invested in R&D
	private static double xi; // share of innovation in R&D expenditure
	private static double zeta1; // parameter in Bernouilli distribution parameter for innovation
	private static double zeta2; // parameter in Bernouilli distribution parameter for imitation
	private static double gamma; // new clients per period as a share of current clients 
	
	// parameters for the distribution
	private static double alpha1; // alpha parameter in Beta distribution for innovation 
	private static double beta1; // beta parameter in Beta distribution for innovation 
	private static double x1lower; // lower support of Beta distribution for innovation 
	private static double x1upper; // upper support of Beta distribution for innovation 
	
	private static double alpha2; // alpha parameter in Beta distribution for innovation of new entrants
	private static double beta2; // alpha parameter in Beta distribution for innovation of new entrants
	
	// C-FIRMS
	private static int dimK;// not specified in the model; represent the max number of machine a firm has 
	private static double maxKGrowth; // ''in any give period firm capital growth rates cannot exceed a fixed maximum threshold''
	
	private static double iota; // desired level of inventories as a share of expected demand 
	private static double cud; // desired level of capacity utilization 
	private static double chi; // parameter in firms' market share equation 

	private static double eta; // maximal age of machines after which they have to be replaced 
	private static double b; // payback parameter 

	private static double v; // parameter in market shares dynamic equation 
	
	private static double omega1; // price relative importance in competitiveness equation
	private static double omega2; // unfilled demand relative importance in competitiveness equation 
	
	private static double phi1; // lower support Uniform distribution, new entrants capital
	private static double phi2; // upper support Uniform distribution, new entrants capital
	private static double phi3; /* lower support Uniform distribution, new entrants liquid assets
	both for C-firms and K-firms */
	private static double phi4; /* upper support Uniform distribution, new entrants liquid assets
	both for C-firms and K-firms */
	
	private static double repaymentShare; // fraction of the debt they repay in each period

	// BANKING
	private static double lambda; // loan-to-value ratio 
	private static double psiD; // markdown on deposit rate 
	private static double psiL; // markup on interest rate 
	
	private static double cbMd; // mark down on bank's deposit at the CB
	
	// WAGE DYNAMICS
	private static double psi1; // labour productivity parameter
	private static double psi2; // inflation/cpi parameter 
	private static double psi3; // unemployment parameter 
	
	// OTHER PARAMETERS
	private static double speedConv; // determine the precision of the optimal solution 
	
	public static void calibration(){
		// initial value from their code
			a0 = 1;
			w0 = 1;
			k0 = 800;
			nw20 = nw10 =  1000;
			ls = 250000;
		
		
		// mostly from Dosi et al. (2013), appendix
			// k-firms
			mu1 = 0.04;
			nu = 0.04;
			xi = 0.50;
			zeta1 = zeta2 = 0.30;
			gamma = 0.50;
					
			alpha1 = 3.;
			beta1 = 3.;
			x1lower = -0.15;
			x1upper = 0.15;
					
			// Dosi et al. (2010)
			alpha2 = 2.;
			beta2 = 4.;
					
			// c-firms
			dimK = 40;
			maxKGrowth = 0.50; // think so...
			
			iota = 0.10;
			cud = 0.75; // not in any papers, obtained from their code
			chi = 1.;
					
			eta = 19.;
			b = 3.;
					
			v = 0.01;
			omega1 = omega2 = 1.;
					
			// obtained in the calibration table, Dosi et al. (2010)
			phi1 = 0.1;
			phi2 = 0.9;
					
			phi3 = 0.1;
			phi4 = 0.9;
			
			repaymentShare = 0.333333;

			// bank
			lambda = 2.;
			psiD = 1.;
			psiL = 0.50;
			
			cbMd = 0.9; //(from their code) 
					
			
			psi1 = 1.;
			psi2 = 0.;
			psi3 = 0.; 
			
			speedConv = 10;
	}
	
	// ---------------------------------------------------------------------
	// Access methods
	// ---------------------------------------------------------------------

	public static double getMu1() {
		return mu1;
	}

	public static double getNu() {
		return nu;
	}

	public static double getXi() {
		return xi;
	}

	public static double getZeta1() {
		return zeta1;
	}

	public static double getZeta2() {
		return zeta2;
	}

	public static double getGamma() {
		return gamma;
	}

	public static double getAlpha1() {
		return alpha1;
	}

	public static double getBeta1() {
		return beta1;
	}

	public static double getX1lower() {
		return x1lower;
	}

	public static double getX1upper() {
		return x1upper;
	}

	public static double getAlpha2() {
		return alpha2;
	}

	public static double getBeta2() {
		return beta2;
	}

	public static double getIota() {
		return iota;
	}

	public static double getCud() {
		return cud;
	}

	public static double getChi() {
		return chi;
	}

	public static double getEta() {
		return eta;
	}

	public static double getB() {
		return b;
	}

	public static double getV() {
		return v;
	}

	public static double getOmega1() {
		return omega1;
	}

	public static double getOmega2() {
		return omega2;
	}

	public static double getPhi1() {
		return phi1;
	}

	public static double getPhi2() {
		return phi2;
	}

	public static double getPhi3() {
		return phi3;
	}

	public static double getPhi4() {
		return phi4;
	}

	public static double getLambda() {
		return lambda;
	}

	public static double getPsiD() {
		return psiD;
	}

	public static double getPsiL() {
		return psiL;
	}

	public static double getPsi1() {
		return psi1;
	}

	public static double getPsi2() {
		return psi2;
	}

	public static double getPsi3() {
		return psi3;
	}

	public static double getA0() {
		return a0;
	}

	public static double getW0() {
		return w0;
	}

	public static double getNw10() {
		return nw10;
	}

	public static double getNw20() {
		return nw20;
	}

	public static double getK0() {
		return k0;
	}

	public static double getLs() {
		return ls;
	}

	public static int getDimK() {
		return dimK;
	}

	public static double getCbMd() {
		return cbMd;
	}

	public static double getMaxKGrowth() {
		return maxKGrowth;
	}

	public static double getRepaymentShare() {
		return repaymentShare;
	}

	public static double getSpeedConv() {
		return speedConv;
	}

}
