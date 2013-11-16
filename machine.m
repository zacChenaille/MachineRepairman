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
     