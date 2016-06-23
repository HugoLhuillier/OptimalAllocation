package inet.model;

//NOTE: the math behind the adjustment methods is presented in resource_allocation.pdf
// whenever write cf + l, this is equivalent to the payment condition. The constraint is that cf + l > 0
// cf + l is computed in the payment(prod, asset remaining, loan prod, loan debt) method

// cf = cash flow (that include the debt to repay), l = part of loan saved to pay back the debt 
// Assumption here that differs from the general KSM model: 
// 		1. Tax on profit = 0
//		2. int. rate on deposit = 0
// 		3. cost of machine and cost of production are stationary, set equal to 1

import inet.data.*;
import microsim.data.db.PanelEntityKey;
import microsim.engine.SimulationEngine;
import microsim.event.EventListener;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class CFirm implements EventListener {
	
	// ---------------------------------------------------------------------
	// Variables
	// ---------------------------------------------------------------------

	@Id
	private PanelEntityKey key = new PanelEntityKey(idCounter++);
	@Transient
	private static long idCounter = 1000000;
	@Transient
	IUModel model;

	// choice variables 
	@Transient
	double dInvE; // desired level of investment
	@Transient
	double invEStar; // possible level of investment | current resources 
	@Transient
	double cInvE; // cost of investment -- in our case, cost of machine = 1 --> cInvE = dInvE / dimension of machines
	@Transient
	double cD; // credit demand 
	@Transient
	double dQ; // desired level of production
	@Transient
	double qStar; // possible level of production | current resources 
	
	@Transient
	double p; // price of a good
	@Transient
	double c; // unit cost of production
	
	@Transient
	double pDem; // past demand -- to replicate as close as possible the code of the general model
	@Transient
	double debt;
	
	// Liquid asset Variables
	@Transient
	double[] nw; // liquid asset; use an array because like this in the general model 
	@Transient
	double nwPrime; // liquid asset remaining after Step 1.

	// Loan variables
	@Transient
	double lBar; // max. credit = borrowing constraint
	@Transient
	double lProd; // part of the loan used to fund production & investment
	@Transient
	double lDebt; // remaining part of the loan devoted to debt payment
	
	// ---------------------------------------------------------------------
	// Constructor
	// ---------------------------------------------------------------------

	public CFirm(){
	}

	public CFirm(IUModel model, int n) {
		this.model = model;
		key = new PanelEntityKey((long) n);

		this.nw = new double[]{SimulationEngine.getRnd().nextDouble() * 10., 1.};

		this.dInvE = 0.;
		this.dQ = 0;
		this.c = 1;
		this.p = (1 + model.pMarkUp) * c;
	}


	// ---------------------------------------------------------------------
	// EventListener
	// ---------------------------------------------------------------------

	public enum Processes {
		Update,
		Inv;	}

	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {
		case Update:
			update();
			break;
		case Inv:
			invUpdated();
			break;
		}
	}

	// ---------------------------------------------------------------------
	// Own methods, general
	// ---------------------------------------------------------------------

	public void update(){ // draw random numbers 
		this.pDem = (double) SimulationEngine.getRnd().nextInt(20);
		this.dQ = (double) pDem - SimulationEngine.getRnd().nextInt(5);  
		this.nw[0] = SimulationEngine.getRnd().nextDouble() * 5;
		this.dInvE = SimulationEngine.getRnd().nextInt(400); 
		this.dInvE = Math.floor(dInvE / Parameters.getDimK()) * Parameters.getDimK();
		this.debt = SimulationEngine.getRnd().nextDouble() * 5;
		this.lBar =  SimulationEngine.getRnd().nextDouble() * 10;
		
		this.lDebt = 0.;
		this.lProd = 0;
	}

	public void invUpdated(){
		System.out.println("FIRM " + this.getKey().getId());

		cInvE = Math.round(dInvE / Parameters.getDimK()) ;

		 System.out.println("Info: " +
		 "\n pDem " + pDem +
		"\n dQ = " + dQ +
		"\n nw = " + nw[0] +
		"\n maxCredit = " + lBar + " or " +
		"\n dInvE = " + dInvE + " which has a cost of " + cInvE +
		"\n debt = " + debt);

		qStar = dQ;
 		invEStar = dInvE;
 		
		 step1(); // compute the qty and inv achievable with current resources; return q* and i* 
		 step2(); // check whether they are compatible with debt repayment

		if(cD > lBar){
			System.out.println("Problem: shoud not be possible");
			this.cD = lBar;
		}

		System.out.println("The final variables are: " + 
					"\n production: " + dQ +
					"\n investment: " + dInvE +
					"\n remaining wealth " + nwPrime +
					"\n credit demand is " + cD);
	}
	
	// ---------------------------------------------------------------------
	// Own methods, sub-methods of invUpdated()
	// ---------------------------------------------------------------------

	public void step1(){
		// compute the Q and I you can achieve with nw and loan at your disposal 		
		this.nwPrime = nw[0];
		double lPrime = lBar;
		
		// production
		if(dQ <= nwPrime) { // if internal funds are sufficient, use them
			nwPrime -= dQ;
		} else if( dQ <= nwPrime + lPrime) { // if internal funds are not sufficient, then ask
			// for credit; desired production remains id.but unsure of the credit it will receive
			lPrime -=  dQ - nwPrime;
			nwPrime = 0.;
		} else { // need to scale down its production plan to the max it could obtain; still unsure about its actual
					// credit
			this.qStar = (nwPrime + lPrime);
			lPrime = 0.;
			nwPrime = 0.;
		}

		//InvE, ~ similar structure as production 
		if(cInvE <= nwPrime) {
			nwPrime -= cInvE;
		} else if(cInvE <= nwPrime + lPrime){
			lPrime -= cInvE - nwPrime;
			nwPrime = 0.;
		} else {
			this.invEStar = Math.floor((nwPrime + lPrime)) * Parameters.getDimK();
			lPrime = 0.;
			nwPrime = 0.;
		}
		
		this.lProd = lBar - lPrime;

		System.out.println("After Step 1., we have : " +
		"\n dQ = " + qStar +
		"\n dInvE = " + invEStar +
		"\n NW remaining = " + nwPrime +
		"\n lProd = " + lProd + 
		"\n credit remaining: " + lPrime);
	}

	public void step2(){
		// if nw' --> could reach full prod and inv plan without using the loan at all
		if(nwPrime > 0){
			System.out.println("nwPrime is POSITIVE. Checking: "
					+ "\n nwPrime " + nwPrime
					+ "\n lProd should be equal to 0" + lProd
					+ "\n and lPrime should be equal to lBar: " + lProd + " = " + lBar); 

			// in the model, underline{r} = 0 ; abstract from it here & tr = 0
			// paymentWLoan = your expected cash flow at the end of the period, prior to pay the debt, including the loan you keep to pay this debt
			double paymentWLoan = payment(qStar, nwPrime, lBar, 0);
			// paymentWLoan = your expected cash flow at the end of the period, prior to pay the debt, w/out any loan
			double paymentWOutWLoan = payment(qStar, nwPrime, 0, 0);
			
			System.out.println("\t Payment w/out loan: " + paymentWOutWLoan +
							"\n payment w/ loan: " + paymentWLoan);

			if(paymentWOutWLoan >= 0){// in this case no need to use any loan, your expected liquid assets are above the debt you have to repay
				
				this.cD = 0; // to be explicit; 
				this.dQ = qStar; // should be the same
				this.dInvE = invEStar; // id.
				System.out.println("Indeed paymentWOutWLoan > 0");
				
			} else if(paymentWLoan > 0){ // means that expected liquid asset > due debt if use the entire loan to repay the debt. 
				// if this is satisfied --> exists lDebt \in (0, lBar], and the closed form of lDebt is give below 
				
				this.lDebt = 1 / (1 - Parameters.getRepaymentShare() - IUModel.r) * ( (Parameters.getRepaymentShare() + IUModel.r) * debt - (p-c) * qStar - nwPrime);
				System.out.println("Indeed paymentWLoan > 0 "
						+ "\n Checking that indeed lProd = 0: " + lProd
						+ "\n Chgecking lDebt " + lDebt + " which should in principle <= " + lBar);
				
				this.dQ = qStar;
				this.dInvE = invEStar;
				this.cD = lDebt;
				
			} else { // i.e. remaining net wealth + total loan are not sufficient to pay back debt
				
				System.out.println("Couldn't pay with full loan and remaining debt.");
				adjustment1();
				
			}
		} else { // entire nw was used, and therefore also used some (all) of the loan  
			
			System.out.println("nwPrime is NIL. Checking: "
					+ "\n nwPrime " + nwPrime
					+ "\n lProd " + lProd + " that should <= lBar " + lBar);
			
			if(lProd == lBar){ // used all the loan to fund prod & inv. Only case where the firm could have been credit rationed,
				// that is invEStar =< dInve && qStar =< dQ 
				
				System.out.println("Full loan was used for prod and inv.");
				this.lDebt = 0;
				
				// cash flow expected at the end of the period, prior to repay the debt -- obviously lDebt = 0
				double paymentExpected = payment(qStar, nwPrime, lDebt, lProd);
				
				System.out.println("Rev expected: " + paymentExpected);
				
				if(paymentExpected >= 0){ // expected liquid assets are sufficient to repay the due debt 
					
					System.out.println("Indeed paymentExpected > 0");
					this.lDebt = 0;
					this.cD = lProd;
					this.dQ = qStar;
					this.dInvE = invEStar;
					
				} else { // expected liquid assets are NOT sufficient to repay the due debt 
					
					System.out.println("lDebt = 0 & it is not sufficient to pay debt -- paymentExpected < 0");
					adjustment2();
					
				}
			} else { // used only part of the loan, hence some remaining to also pay the debt if needed. Obiously qStar = dQ, id. inv
				
				// as before, see whether if use the totality of the remaining loan makes that the payment condition will be satisfied;
				// if yes, then implies that lDebt \in (0, lBar - lProd] ; o.w. have to adjust 
				this.lDebt = lBar - lProd;
				double paymentWLoan = payment(qStar, nwPrime, lDebt, lProd);
				System.out.println("Only part of the loan was used; if use total loan, get paymentWLoan = " + paymentWLoan);
				
				if(paymentWLoan > 0){  // payment condition hold with full loan used
					
					System.out.println("Indeed paymentWLoan > 0");
					// lDebt \in (0, lBar - lProd]
					this.lDebt = 1/(1 - Parameters.getRepaymentShare() - IUModel.r) * ((Parameters.getRepaymentShare() + IUModel.r) * (debt + lProd) - (p - c) * qStar);
					System.out.println("lDebt is equal to " + lDebt + " and should be < lbar: " + lBar);
					this.cD = lProd + lDebt; 
					System.out.println("cD should also be inferior to lBar " + cD + " < " + lBar);
					this.dQ = qStar; // in this case should be equal 
					this.dInvE = invEStar; // id.
					
				} else { // payment condition does not hold with full loan used -- obv. will not hold with lower loan 
					
					System.out.println("The remaining loan was not sufficient");
					
					adjustment2();
					
				}
			}
		}
	}
	
	// ---------------------------------------------------------------------
	// Own methods, adjustment processes
	// ---------------------------------------------------------------------
	
	public void adjustment1(){
		
		System.out.println("Reach the phase of adjustment 1");
		// expected cash flow is mono. increasing in lDebt --> set lDebt = max loan
		this.lDebt = lBar;
		this.lProd = 0;
		System.out.println("Payment condition prior to start the adj. is " + payment(qStar, nwPrime, lDebt, lProd));
		
		// Incrementally reduce investment that was funded through internal fund --> use the money saved from the inv. to increase the deposit at the bank
		while(payment(qStar, nwPrime, lDebt, lProd) < 0 && invEStar > 0){ 
			
			invEStar -= Parameters.getDimK(); // reduce inv. by one machine 
			nwPrime += 1; // because the cost  of a machine = 1
			
			System.out.println("Checking adjust. process: " +
						"\n invEStar; " + invEStar +
						"\n nwPrime: " + nwPrime +
						"\n payment: " + payment(qStar, nwPrime, lDebt, lProd));
			
			if(payment(qStar, nwPrime, lDebt, lProd) >= 0){ // if yes, then updates the variables with their final values and the adjustment process stops 
				
				this.cD = lDebt;
				this.dInvE = invEStar;
				this.dQ = qStar;
				
				System.out.println("Adjustment succeeded: payment > 0");
				
			}
		}
		
		//if payment is still negative, have to continue the adjustment
		if(payment(qStar, nwPrime, lDebt, lProd) < 0){
			
			System.out.println("Inv. adjustment was not sufficient to make payment > 0");
			this.dInvE = 0;
			
			if(p - c < 1){ // see the pdf for a complete explanation; basically: return to savings > return to production 
				// --> reducing production & savings more will bring the firm closer to its payment restriction. 
				// By the mean value theorem, exist q* s.t. payment condition is satisfied (here qH). Only
				// question is whether qH > 0
				
				// level of quantity for which E(cf + l) = 0
				double qH = 1 / (p - c - 1) * ((Parameters.getRepaymentShare() + IUModel.r) * debt - nw[0] - (1 - Parameters.getRepaymentShare() - IUModel.r) * lDebt);
				
				System.out.println("cf + l is decreasing in q --> exist qH*. Yet, qH is : " + qH);
				
				if(qH > 0){ // if positive, then means that the adjustment is possible 
					
					this.dQ = qH;
					this.nwPrime = nw[0] - dQ; //TODO: change with cost of production different than one
					this.cD = lDebt;
					
					System.out.println("Indeed qH > 0  --> nwPrime = " + nwPrime +
							"\n Checking that indeed payment > 0: " + payment(dQ, nwPrime, lDebt, lProd));
					
				} else {
					
					// cannot adjust. Ass: try to min the losses (e.g. o.w. have to incur more fees, or judiciary costs etc.)
					// because cf + l is mono decreasing in q (as all pounds used for production = pounds not used for savings
					// and return to savings > return to production) --> set q = 0
					this.dQ = 0;
					this.nwPrime = nw[0];
					this.lDebt = 0; // this will shift down the curve but do not take riskier position // not leverage more (ass) 
					this.cD = 0;
					
					System.out.println("Indeed qH < 0 --> nwPrime = nw[0] = " + nw[0] +
							"\n Checking that indeed payment < 0: " + payment(dQ, nwPrime, lDebt, lProd));
					
				}
			} else { // here cf + l is increasing in the production, s.t. if cf + l < 0 at the optimal quantity, 
				// there is now to adjust -- increasing production will not increase revenues because could not sell
				// this production (sales are bounded above by demand). If try to min the losses, then have to set dQ = qStar;
				
				this.dQ = qStar; // id.
				this.lDebt = 0; // this will shift down the curve but cannot leverage more (ass) 
				this.cD = 0;
				this.nwPrime = nw[0] - qStar;
				
				System.out.println("cf + l is increasing in q --> no way out, hence dQ = " + dQ + " and nwPrime " + nwPrime + 
						"\n checking that indeed payment < 0: " + payment(dQ, nwPrime, lDebt, lProd));
			
			}
		}
	}
	
	public void adjustment2(){
		// once more cf + l is increasing in lDebt --> set lDebt at the max you can. Obviously if lProd = lBar --> lDebt = 0
		this.lDebt = lBar - lProd;
		if(lDebt < 0) // should never be the case
			this.lDebt = 0;
		
		// payment equation
		System.out.println("Reach the adj2. Prior to start the adj, payment condition is given by " + payment(qStar, nwPrime, lDebt, lProd));
		
		//ass: 1 >= Parameters.getRepaymentShare() + IUModel.r
		while(payment(qStar, nwPrime, lDebt, lProd) < 0 && invEStar > 0){ // reduces incrementally inv. up to either --> 0 or the payment condition is satisfied 
			
			if(lProd > 0){
				
				System.out.println("Inv. so far funded through loan --> re-allocate loan from lProd to lDebt");
				// re-allocate from lProd to lDebt -- because inv. funded with loan
				
				if(lProd > 1){ // need to take this into account because inv. could be partially fund with loan & liquid asset 
					// here means that this machine was fully fund with loan
					this.lProd -= 1;
					
					this.lDebt += 1;
				} else { // marginal case; some reduction --> increase lDebt && the rest : increase in nwPrime
					// hence here, case where lProd reaches 0 before inv
					
					// part of the machine not funded with loan but with nw
					double savings = 1 - lProd;
					this.nwPrime += savings;
					this.lProd = 0;
					this.lDebt = lBar;
					
				}
			} else {
				// re-allocate from inv to nwPrime -- because inv. funded with nw
				System.out.println("Inv. funded through internal funds --> re-allocate lthose funds to savings");
				
				this.nwPrime +=1;
				
			}
			this.invEStar -= Parameters.getDimK();
			
			System.out.println("Checking adjust. process: " +
					"\n invEStar; " + invEStar +
					"\n lProd " + lProd +
					"\n lDebt " + lDebt +
					"\n nwPrime: " + nwPrime +
					"\n payment: " + payment(qStar, nwPrime, lDebt, lProd));
			
			//re-compute the payment equation. If positive --> update the variables with their final values & then stop
			if(payment(qStar, nwPrime, lDebt, lProd) > 0){
				
				this.dQ = qStar;
				this.dInvE = invEStar;
				this.cD = lDebt + lProd;
				
				System.out.println("Adjustment succeeded: payment > 0");
				// nwPrime already be defined earlier 
				
			}
		} 
		
		// if payment are still negative, then needs to adjust further through quantities
		if(payment(qStar, nwPrime, lDebt, lProd) < 0){
			
			this.dInvE = 0;
			System.out.println("Adjustment through inv. not sufficient --> adjust through qty as well");
			
			if(lProd > 0){ // if lProd > 0 --> some part of the prod was also founded through loan
				
				if(p - c < 1){ 
					// as for adjustment 1(), means that cf + l is (mono) decreasing in quantity --> mean value theorem
					// imply that there exist qH s.t. cf + l = 0, but qH could be > 0 or < 0

					// slightly different case here: break in the slope of cf + l, when lProd > 0 & lProd = 0 (see pdf)
					// hence need to study if the solution is with lProd > 0 (in which case qH > 0) or if it is in the lP = 0,
					// in which case not sure if qH <> 0
					
					// first try to see whether cf + l > 0 with lProd = 0
					// s.t. locate ourself at the breaking point between the two part of the curve 
					this.lDebt = lBar;
					
					// No loan available --> by definition the corresponding level of qty = either optimal one (dQ) or the one
					// that the firm is able to fund (nw[0] -- with cost of production = 1). In theory = nw[0] -- because lProd > 0 initially
					double qH = Math.min(dQ, nw[0]); // should change with cost different than one
					nwPrime = nw[0] - qH; // should be equal to 0
					
					System.out.println("Payment condition is decreasing in dQ --> reduce it. Try at point where the slope is changing: "
							+ "\n qH is : " + qH 
							+ "\n and payment: " + payment(qH, nwPrime, lDebt, lProd));
					
					if(payment(qH, nwPrime, lDebt, lProd) >=0){ 
						// then implies that the allocation solution is to the right of lProd = 0
						// closed form solution, dQ : cf + l = 0
						this.dQ = 1 / (p - c - 1) * ((Parameters.getRepaymentShare() + IUModel.r) * debt - nw[0] - lBar * ( 1 - Parameters.getRepaymentShare() - IUModel.r));
						// this dQ should be positive 

						this.lProd = Math.max(0, qH - nw[0]);
						// should be positive
						this.lDebt = lBar - lProd;
						this.cD = lBar; // because was just pure re-allocation between lProd and lDebt 

						this.nwPrime = Math.max(0, nw[0] - dQ);
						//nwPrime should be equal to 0
						
						System.out.println("Indeed payment > 0 --> have to find close form solution for q*. We have: " +
								"\n qH = " + dQ + "should be <= original dQ" +
								"\n corres. lProd: " + lProd +
								"\n corres. lDebt: " + lDebt +
								"\n corres. nwPrime (should be 0) " + nwPrime +
								"\n and payment condition should be > 0 = " + payment(dQ, nwPrime, lDebt, lProd));
				
					} else { // means that the solution is to the left of this point; then not sure whether qH > 0 or qH < 0
						
						this.lProd = 0; // all the loan is used to fund debt repayment, not production
						this.lDebt = lBar;
						// closed form for qH : cf + l = 0
						
						qH = 1 / (p - c - 1) * ((Parameters.getRepaymentShare() + IUModel.r) * debt - nw[0] - lDebt * (1 - Parameters.getRepaymentShare() - IUModel.r));
					
						System.out.println("Solution is actually to the left of this point. Optimal production: " + qH);
						if(qH > 0){ // check whether indeed qH is positive 
						
							this.dQ = qH;
							this.cD = lDebt;
						
							System.out.println("Indeed qH > 0 --> payment should be = 0 : " + payment(dQ, nwPrime, lDebt, lProd));
						} else { // if not possible, means that cannot reach the point where cf + l = 0
							// has to opt out for the safest strategy: min its loss and cf + l is (mono) decreasing in dQ --> dQ = 0
							this.dQ = 0;
							this.nwPrime = nw[0]; // all liquid assets are left in the deposit (yield higher return than production)
							this.cD = this.lDebt = this.lProd = 0;
							
							System.out.println("qH w 0 --> payment should be < 0 : " + payment(dQ, nwPrime, lDebt, lProd));
						
						}
					}
				} else {
					// here depends on the other derivative ; however, because in this model r (depo) = 0, they are id. 
					//TODO: change when put it into the general model (could be that cf + l is not mono increasing in dQ)
					
					//therefore here no way to adjust, cf + l is mono increasing in dQ : want to min the loss --> dQ = optimal plan
					this.dQ = qStar;
					this.cD = this.lProd = this.lDebt = 0;
					this.nwPrime = Math.max(0, nw[0] - dQ); // should be equal to 0, production yields higher return than savings 
					
					System.out.println("cf + l is increasing in dQ --> no way to adjust. Payment < 0 : " + payment(dQ, nwPrime, lDebt, lProd));
				
				}
			} else { // All the prod. was funded through liquid asset --> id. to loop 1, second bit 
				
				this.lDebt = lBar;
				if(p - c < 1){ // see the pdf for a complete explanation; basically: return to savings > return to production 
					// --> reducing production & savings more will bring the firm closer to its payment restriction. 
					// By the mean value theorem, exist q* s.t. payment condition is satisfied (here qH). Only
					// question is whether qH > 0
					
					// level of quantity for which E(cf + l) = 0
					double qH = 1 / (p - c - 1) * ((Parameters.getRepaymentShare() + IUModel.r) * debt - nw[0] - (1 - Parameters.getRepaymentShare() - IUModel.r) * lDebt);
					System.out.println("cf + l is decreasing in q --> exist qH*. Yet, qH is : " + qH);
					
					if(qH > 0){ // if positive, then means that the adjustment is possible 
					
						this.dQ = qH;
						this.nwPrime = nw[0] - dQ; //TODO: change with cost of production different than one
						this.cD = lDebt;
						
						System.out.println("Indeed qH > 0  --> nwPrime = " + nwPrime +
								"\n Checking that indeed payment > 0: " + payment(dQ, nwPrime, lDebt, lProd));
					
					} else {
						// cannot adjust. Ass: try to min the losses (e.g. o.w. have to incur more fees, or judiciary costs etc.)
						// because cf + l is mono decreasing in q (as all pounds used for production = pounds not used for savings
						// and return to savings > return to production) --> set q = 0
						this.dQ = 0;
						this.nwPrime = nw[0];
						this.lDebt = 0; // this will shift down the curve but do not take riskier position // not leverage more (ass) 
						this.cD = 0;
						
						System.out.println("Indeed qH < 0 --> nwPrime = nw[0] = " + nw[0] +
								"\n Checking that indeed payment < 0: " + payment(dQ, nwPrime, lDebt, lProd));
					
					}
				} else { // here cf + l is increasing in the production, s.t. if cf + l < 0 at the optimal quantity, 
					// there is now to adjust -- increasing production will not increase revenues because could not sell
					// this production (sales are bounded above by demand). If try to min the losses, then have to set dQ = qStar;
					this.dQ = qStar; // id.
					this.lDebt = 0; // this will shift down the curve but cannot leverage more (ass) 
					this.cD = 0;
					this.nwPrime = nw[0] - qStar;
					
					System.out.println("cf + l is increasing in q --> no way out, hence dQ = " + dQ + " and nwPrime " + nwPrime + 
							"\n checking that indeed payment < 0: " + payment(dQ, nwPrime, lDebt, lProd));
				
				}
			}
		}
	}
	
	// metho that compute the payment condition as a function of the loan, production & liquid asset remaining 
	public double payment(double q, double nw, double lD, double lP){
		
		double payment = (p-c)*q + lD *(1 - Parameters.getRepaymentShare() - IUModel.r) + nw - (Parameters.getRepaymentShare() + IUModel.r) * (debt + lP);
		return payment;
		
	}
	// ---------------------------------------------------------------------
	// Access methods
	// ---------------------------------------------------------------------

	public PanelEntityKey getKey() {
		return key;
	}

}
