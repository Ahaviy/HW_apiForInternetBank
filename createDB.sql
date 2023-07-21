CREATE database projectbase;

CREATE TABLE public.balance
(
	id SERIAL PRIMARY KEY,
	amount numeric(14, 2)
);

CREATE TABLE public.operation_type
(
	id SERIAL PRIMARY KEY,
	operation_name VARCHAR(10) NOT NULL
);

CREATE TABLE public.history_of_operation
(
	id SERIAL PRIMARY KEY,
	balance_id integer NOT NULL,
	operation_type integer NOT NULL,
	amount numeric(14, 2) NOT NULL,
	datetime TIMESTAMPTZ DEFAULT NOW(),
	CONSTRAINT type_id FOREIGN KEY (operation_type)
		REFERENCES public.operation_type (id),
	CONSTRAINT balance_id FOREIGN KEY (balance_id)
	REFERENCES public.balance (id) 
);

insert into public.operation_type (operation_name) values
	('put'),
	('withdraw');









