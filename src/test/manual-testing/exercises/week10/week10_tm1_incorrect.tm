states: q0 q1 q2 q_accept q_reject
input_alphabet: 0 1
start: q0
accept: q_accept
REJECT: q_reject
tape_alphabet: 0 1 _

transitions:
q0 1 -> q1 1 R

q0 _ -> q_accept _ R
q1 0 -> q1 0 R
q1 1 -> q2 1 R
q1 _ -> q_reject _ R
q2 0 -> q2 0 R
q2 1 -> q0 1 R
q2 _ -> q_reject _ R
