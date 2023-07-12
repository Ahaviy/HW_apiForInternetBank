CREATE database projectbase;

CREATE TABLE public.balance
(
	id integer NOT NULL,
	amount numeric(14, 2),
	PRIMARY KEY (id)
);

INSERT INTO public.balance (id, amount) values
(1, 850),
(2, 1000.58),
(3, 1120),
(4, 1185.34);


