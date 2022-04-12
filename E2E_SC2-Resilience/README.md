# MARE
This is a technical report for MARE, a semantic disruption management and resilience evaluation framework, 
to integrate data covered by all disruption management process steps: Monitor/Model, Assess, Recover and Evaluate. 

----------------------------------------------------------------------------------------------------------------------------------------------------------------
Installation on a local machine:
--------------------------------

 The code for MARE is available on the github repository https://github.com/NourRamzy/MARE--Resilience-Framework as a maven project. 
 A simple pull request into a JAVA supporting IDE makes MARE accessible. 
----------------------------------------------------------------------------------------------------------------------------------------------------------------
Resources: 
--------------------------------
In order to examine the disruptions effect, we rely on the data generated and provided by the synthetic generator described in the technical report https://github.com/NourRamzy/E2E_SC. 
We assume the pre-existence of a synthetic SC with the orders, their corresponding supply plans and time frames. The input file is in src/main/java/supply-chain.ttl

----------------------------------------------------------------------------------------------------------------------------------------------------------------
Code Structure: 
--------------------------------

• Model:  in order to model disruptions we create the methodcreate_disruptions(). 
 It implements the required disruptions with the corresponding characteristics, e.g., hasScope,hasSeverity, hasBeginDate, hasEndDateandhasLocation.

• Assess: As described in MARE, in order to assess the consequences of disruptions, first, we retrieve the affected nodes, i.e., fall within the disruption location and time frame. 
The methodget_disrupted_plans()consists of the SPARQL query to retrieve the disrupted supply plans for the affected nodes.

• Recover: We create for each supply plan different alternatives based on the recovery strategies implemented by the functionstry_strategic_stock(), try_alternative_mode(), try_later_recovery().
Afterward, we executeadd_rest_plan()to ensure that the non-disrupted parts of the plan are modeled as we link these new plans to the order. 
In case of an external disruption, we implement thetry_alternative_suppliers()to find alternative suppliers to provide the same intermediate products or materials, for the same time as the disrupted supplier.
In case it is successful, we executeallocate_supplier_product(), propagate_capacity()to  reflect the new capacities allocated to compensate for the disrupted supply.

• Evaluate: For each order, we create alternative plans. 
In order to evaluate the disruption, we execute get_plan_quantity()to evaluate if the final quantity in the supply plan is equivalent to the pre-disruption quantities.
Similarly, get_plan_price()andget_latest_plan_time()are to calculate the price and the delivery time of the alternative plans; 
The result of the previous methods can be aggregated per customer or for the overall orders

 