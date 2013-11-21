% Zac Chenaille
% CDA 6530 - Discrete Event Machine Repairman Theoretical Results
% 11/16/2013

lambda = 0.4;
mu = 0.6;

Q = [-mu    mu           0                0                0;
     lambda -(mu+lambda) mu               0                0;
     0      2*lambda     -(mu+(2*lambda)) mu               0;
     0      0            3*lambda         -(mu+(3*lambda)) mu;
     0      0            0                4*lambda         -4*lambda];
	 
Pi_theory = zeros(1,5);
Q_m = [Q(:, 1:4) ones(5,1)];
B = [0 0 0 0 1];
Pi_theory = B * inv(Q_m);

states = [0 1 2 3 4];
% Display graphs using the analytical results as well as the data from the real simulation
Pi100Machines = [0.17029 0.34015 0.26344 0.15232 0.07377];

figure;
plot(states, Pi_theory, "b-o");
hold on;
plot(states, Pi100Machines, "k-^");
title("Analytical vs Actual Steady-State Probabilities: 100 Machines");
xlabel("# Working Machines");
ylabel("Steady-State Probability");
legend("Analytical", "Actual", "location", "southwest");

Pi10000Machines = [0.22747 0.34166 0.25741 0.12682 0.04661];

figure;
plot(states, Pi_theory, "b-o");
hold on;
plot(states, Pi10000Machines, "k-^");
title("Analytical vs Actual Steady-State Probabilities: 10,000 Machines");
xlabel("# Working Machines");
ylabel("Steady-State Probability");
legend("Analytical", "Actual", "location", "southwest");
     