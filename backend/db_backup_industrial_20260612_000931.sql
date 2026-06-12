--
-- PostgreSQL database dump
--

\restrict oLh7rDofNYe5EQugae4RgS5IrN6fmvG3TtQXyBSY4e3i3YxLbSUn1g0htSofXg2

-- Dumped from database version 18.3 (Homebrew)
-- Dumped by pg_dump version 18.3 (Homebrew)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: customer; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.customer (
    id bigint NOT NULL,
    active boolean,
    code character varying(50) NOT NULL,
    contact_name character varying(255),
    created_at timestamp(6) without time zone,
    credit_limit numeric(15,2),
    current_balance numeric(15,2),
    email character varying(255),
    name character varying(255) NOT NULL,
    payment_terms integer,
    phone character varying(50),
    tax_code character varying(50),
    updated_at timestamp(6) without time zone
);


ALTER TABLE public.customer OWNER TO postgres;

--
-- Name: customer_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.customer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.customer_id_seq OWNER TO postgres;

--
-- Name: customer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.customer_id_seq OWNED BY public.customer.id;


--
-- Name: delivery_address; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.delivery_address (
    id bigint NOT NULL,
    address_line1 character varying(255) NOT NULL,
    address_line2 character varying(255),
    address_name character varying(255),
    city character varying(100),
    country character varying(100),
    created_at timestamp(6) without time zone,
    is_default boolean,
    notes character varying(500),
    phone character varying(50),
    postal_code character varying(20),
    recipient_name character varying(255),
    state character varying(100),
    updated_at timestamp(6) without time zone,
    customer_id bigint NOT NULL
);


ALTER TABLE public.delivery_address OWNER TO postgres;

--
-- Name: delivery_address_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.delivery_address_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.delivery_address_id_seq OWNER TO postgres;

--
-- Name: delivery_address_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.delivery_address_id_seq OWNED BY public.delivery_address.id;


--
-- Name: delivery_order; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.delivery_order (
    id bigint NOT NULL,
    code character varying(255),
    destination_address character varying(255),
    status character varying(255),
    sales_order_id bigint
);


ALTER TABLE public.delivery_order OWNER TO postgres;

--
-- Name: delivery_order_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.delivery_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.delivery_order_id_seq OWNER TO postgres;

--
-- Name: delivery_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.delivery_order_id_seq OWNED BY public.delivery_order.id;


--
-- Name: delivery_plan; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.delivery_plan (
    id bigint NOT NULL,
    code character varying(255),
    created_date date,
    description character varying(255),
    status character varying(255)
);


ALTER TABLE public.delivery_plan OWNER TO postgres;

--
-- Name: delivery_plan_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.delivery_plan_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.delivery_plan_id_seq OWNER TO postgres;

--
-- Name: delivery_plan_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.delivery_plan_id_seq OWNED BY public.delivery_plan.id;


--
-- Name: delivery_plan_order; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.delivery_plan_order (
    id bigint NOT NULL,
    delivery_order_id bigint,
    delivery_plan_id bigint
);


ALTER TABLE public.delivery_plan_order OWNER TO postgres;

--
-- Name: delivery_plan_order_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.delivery_plan_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.delivery_plan_order_id_seq OWNER TO postgres;

--
-- Name: delivery_plan_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.delivery_plan_order_id_seq OWNED BY public.delivery_plan_order.id;


--
-- Name: delivery_plan_shipper; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.delivery_plan_shipper (
    id bigint NOT NULL,
    phone character varying(255),
    shipper_name character varying(255),
    delivery_plan_id bigint
);


ALTER TABLE public.delivery_plan_shipper OWNER TO postgres;

--
-- Name: delivery_plan_shipper_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.delivery_plan_shipper_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.delivery_plan_shipper_id_seq OWNER TO postgres;

--
-- Name: delivery_plan_shipper_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.delivery_plan_shipper_id_seq OWNED BY public.delivery_plan_shipper.id;


--
-- Name: delivery_triproute; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.delivery_triproute (
    id bigint NOT NULL,
    code character varying(255),
    completed_at timestamp(6) without time zone,
    notes character varying(500),
    shipper_name character varying(255),
    started_at timestamp(6) without time zone,
    status character varying(50),
    delivery_plan_id bigint,
    shipper_user_id bigint,
    CONSTRAINT delivery_triproute_status_check CHECK (((status)::text = ANY (ARRAY[('CREATED'::character varying)::text, ('IN_PROGRESS'::character varying)::text, ('COMPLETED'::character varying)::text, ('CANCELLED'::character varying)::text])))
);


ALTER TABLE public.delivery_triproute OWNER TO postgres;

--
-- Name: delivery_triproute_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.delivery_triproute_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.delivery_triproute_id_seq OWNER TO postgres;

--
-- Name: delivery_triproute_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.delivery_triproute_id_seq OWNED BY public.delivery_triproute.id;


--
-- Name: delivery_triproute_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.delivery_triproute_item (
    id bigint NOT NULL,
    sequence integer,
    status character varying(255),
    delivery_order_id bigint,
    triproute_id bigint
);


ALTER TABLE public.delivery_triproute_item OWNER TO postgres;

--
-- Name: delivery_triproute_item_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.delivery_triproute_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.delivery_triproute_item_id_seq OWNER TO postgres;

--
-- Name: delivery_triproute_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.delivery_triproute_item_id_seq OWNED BY public.delivery_triproute_item.id;


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO postgres;

--
-- Name: goods_issue; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.goods_issue (
    id bigint NOT NULL,
    carrier_name character varying(100),
    code character varying(50) NOT NULL,
    confirmed_by bigint,
    confirmed_date timestamp(6) without time zone,
    created_at timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    delivery_note_number character varying(100),
    issue_date date NOT NULL,
    notes character varying(500),
    shipping_method character varying(100),
    status character varying(30) NOT NULL,
    total_amount numeric(15,2),
    tracking_number character varying(100),
    updated_at timestamp(6) without time zone,
    delivery_address_id bigint,
    sales_order_id bigint NOT NULL,
    warehouse_id bigint,
    CONSTRAINT goods_issue_status_check CHECK (((status)::text = ANY (ARRAY[('DRAFT'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANCELLED'::character varying)::text])))
);


ALTER TABLE public.goods_issue OWNER TO postgres;

--
-- Name: goods_issue_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.goods_issue_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.goods_issue_id_seq OWNER TO postgres;

--
-- Name: goods_issue_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.goods_issue_id_seq OWNED BY public.goods_issue.id;


--
-- Name: goods_issue_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.goods_issue_item (
    id bigint NOT NULL,
    batch_number character varying(100),
    expiry_date date,
    issued_quantity integer NOT NULL,
    notes character varying(500),
    ordered_quantity integer NOT NULL,
    total_amount numeric(15,2),
    unit character varying(50),
    unit_price numeric(15,2),
    goods_issue_id bigint NOT NULL,
    product_id bigint NOT NULL,
    sales_order_item_id bigint NOT NULL
);


ALTER TABLE public.goods_issue_item OWNER TO postgres;

--
-- Name: goods_issue_item_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.goods_issue_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.goods_issue_item_id_seq OWNER TO postgres;

--
-- Name: goods_issue_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.goods_issue_item_id_seq OWNED BY public.goods_issue_item.id;


--
-- Name: goods_receipt; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.goods_receipt (
    id bigint NOT NULL,
    code character varying(50) NOT NULL,
    confirmed_by bigint,
    confirmed_date timestamp(6) without time zone,
    created_at timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    delivery_note_number character varying(100),
    invoice_number character varying(100),
    notes character varying(500),
    receipt_date date NOT NULL,
    status character varying(30) NOT NULL,
    total_amount numeric(15,2),
    updated_at timestamp(6) without time zone,
    purchase_order_id bigint NOT NULL,
    warehouse_id bigint,
    CONSTRAINT goods_receipt_status_check CHECK (((status)::text = ANY (ARRAY[('DRAFT'::character varying)::text, ('CONFIRMED'::character varying)::text, ('CANCELLED'::character varying)::text])))
);


ALTER TABLE public.goods_receipt OWNER TO postgres;

--
-- Name: goods_receipt_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.goods_receipt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.goods_receipt_id_seq OWNER TO postgres;

--
-- Name: goods_receipt_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.goods_receipt_id_seq OWNED BY public.goods_receipt.id;


--
-- Name: goods_receipt_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.goods_receipt_item (
    id bigint NOT NULL,
    accepted_quantity integer NOT NULL,
    batch_number character varying(100),
    expiry_date date,
    notes character varying(255),
    ordered_quantity integer NOT NULL,
    received_quantity integer NOT NULL,
    rejected_quantity integer,
    rejection_reason character varying(255),
    total_amount numeric(15,2),
    unit character varying(50),
    unit_price numeric(15,2),
    goods_receipt_id bigint NOT NULL,
    product_id bigint NOT NULL,
    purchase_order_item_id bigint NOT NULL
);


ALTER TABLE public.goods_receipt_item OWNER TO postgres;

--
-- Name: goods_receipt_item_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.goods_receipt_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.goods_receipt_item_id_seq OWNER TO postgres;

--
-- Name: goods_receipt_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.goods_receipt_item_id_seq OWNED BY public.goods_receipt_item.id;


--
-- Name: inventory; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.inventory (
    id bigint NOT NULL,
    average_cost numeric(15,2),
    last_issued_date timestamp(6) without time zone,
    last_received_date timestamp(6) without time zone,
    quantity_available integer NOT NULL,
    quantity_on_hand integer NOT NULL,
    quantity_reserved integer NOT NULL,
    reorder_level integer,
    reorder_quantity integer,
    updated_at timestamp(6) without time zone,
    version bigint,
    product_id bigint NOT NULL,
    warehouse_id bigint NOT NULL
);


ALTER TABLE public.inventory OWNER TO postgres;

--
-- Name: inventory_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.inventory_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.inventory_id_seq OWNER TO postgres;

--
-- Name: inventory_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.inventory_id_seq OWNED BY public.inventory.id;


--
-- Name: inventory_lot; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.inventory_lot (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    expiry_date date,
    lot_number character varying(64) NOT NULL,
    manufacture_date date,
    quantity_received numeric(18,3) NOT NULL,
    quantity_remaining numeric(18,3) NOT NULL,
    unit_cost numeric(18,2),
    updated_at timestamp(6) without time zone NOT NULL,
    product_id bigint NOT NULL,
    source_receipt_id bigint,
    source_receipt_item_id bigint,
    warehouse_id bigint NOT NULL
);


ALTER TABLE public.inventory_lot OWNER TO postgres;

--
-- Name: inventory_lot_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.inventory_lot_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.inventory_lot_id_seq OWNER TO postgres;

--
-- Name: inventory_lot_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.inventory_lot_id_seq OWNED BY public.inventory_lot.id;


--
-- Name: inventory_reservation; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.inventory_reservation (
    id bigint NOT NULL,
    product_id bigint NOT NULL,
    warehouse_id bigint NOT NULL,
    sales_order_id bigint NOT NULL,
    sales_order_item_id bigint NOT NULL,
    reserved_quantity integer NOT NULL,
    fulfilled_quantity integer DEFAULT 0 NOT NULL,
    status character varying(30) DEFAULT 'RESERVED'::character varying NOT NULL,
    reserved_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expires_at timestamp without time zone,
    released_at timestamp without time zone,
    notes text
);


ALTER TABLE public.inventory_reservation OWNER TO postgres;

--
-- Name: inventory_reservation_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.inventory_reservation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.inventory_reservation_id_seq OWNER TO postgres;

--
-- Name: inventory_reservation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.inventory_reservation_id_seq OWNED BY public.inventory_reservation.id;


--
-- Name: inventory_transaction; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.inventory_transaction (
    id bigint NOT NULL,
    created_by bigint,
    notes character varying(255),
    quantity integer NOT NULL,
    quantity_after integer,
    quantity_before integer,
    reference_code character varying(50),
    reference_id bigint,
    reference_type character varying(50),
    total_cost numeric(15,2),
    transaction_date timestamp(6) without time zone NOT NULL,
    transaction_type character varying(30) NOT NULL,
    unit_cost numeric(15,2),
    product_id bigint NOT NULL,
    warehouse_id bigint NOT NULL,
    CONSTRAINT inventory_transaction_transaction_type_check CHECK (((transaction_type)::text = ANY (ARRAY[('RECEIPT'::character varying)::text, ('ISSUE'::character varying)::text, ('TRANSFER_IN'::character varying)::text, ('TRANSFER_OUT'::character varying)::text, ('ADJUSTMENT_PLUS'::character varying)::text, ('ADJUSTMENT_MINUS'::character varying)::text, ('RETURN_IN'::character varying)::text, ('RETURN_OUT'::character varying)::text])))
);


ALTER TABLE public.inventory_transaction OWNER TO postgres;

--
-- Name: inventory_transaction_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.inventory_transaction_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.inventory_transaction_id_seq OWNER TO postgres;

--
-- Name: inventory_transaction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.inventory_transaction_id_seq OWNED BY public.inventory_transaction.id;


--
-- Name: invoice; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.invoice (
    id bigint NOT NULL,
    code character varying(255),
    invoice_date date,
    status character varying(255),
    total_amount double precision,
    purchase_order_id bigint,
    supplier_id bigint
);


ALTER TABLE public.invoice OWNER TO postgres;

--
-- Name: invoice_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.invoice_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.invoice_id_seq OWNER TO postgres;

--
-- Name: invoice_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.invoice_id_seq OWNED BY public.invoice.id;


--
-- Name: product; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.product (
    id bigint NOT NULL,
    code character varying(255) NOT NULL,
    description character varying(255),
    name character varying(255) NOT NULL,
    price double precision,
    quantity integer,
    supplier_id bigint
);


ALTER TABLE public.product OWNER TO postgres;

--
-- Name: product_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.product_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.product_id_seq OWNER TO postgres;

--
-- Name: product_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.product_id_seq OWNED BY public.product.id;


--
-- Name: purchase_order; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.purchase_order (
    id bigint NOT NULL,
    approved_by bigint,
    approved_date timestamp(6) without time zone,
    code character varying(50) NOT NULL,
    completed_date timestamp(6) without time zone,
    created_by bigint,
    created_date date NOT NULL,
    delivery_date timestamp(6) without time zone,
    invoice_number character varying(100),
    notes character varying(500),
    order_name character varying(255),
    rejection_reason character varying(500),
    shipping_cost numeric(15,2),
    status character varying(50) NOT NULL,
    tax_type character varying(50),
    total_amount numeric(15,2),
    updated_at timestamp(6) without time zone,
    supplier_id bigint NOT NULL,
    warehouse_id bigint,
    CONSTRAINT purchase_order_status_check CHECK (((status)::text = ANY (ARRAY[('ORDER_OPEN'::character varying)::text, ('ORDER_APPROVED'::character varying)::text, ('ORDER_PARTIALLY_RECEIVED'::character varying)::text, ('ORDER_COMPLETED'::character varying)::text, ('ORDER_CANCELLED'::character varying)::text])))
);


ALTER TABLE public.purchase_order OWNER TO postgres;

--
-- Name: purchase_order_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.purchase_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.purchase_order_id_seq OWNER TO postgres;

--
-- Name: purchase_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.purchase_order_id_seq OWNED BY public.purchase_order.id;


--
-- Name: purchase_order_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.purchase_order_item (
    id bigint NOT NULL,
    amount_before_tax numeric(15,2),
    cost_before_tax numeric(15,2),
    notes character varying(255),
    quantity integer NOT NULL,
    received_quantity integer NOT NULL,
    tax_amount numeric(15,2),
    total_amount numeric(15,2),
    unit character varying(50),
    unit_price numeric(15,2) NOT NULL,
    product_id bigint NOT NULL,
    purchase_order_id bigint NOT NULL
);


ALTER TABLE public.purchase_order_item OWNER TO postgres;

--
-- Name: purchase_order_item_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.purchase_order_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.purchase_order_item_id_seq OWNER TO postgres;

--
-- Name: purchase_order_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.purchase_order_item_id_seq OWNED BY public.purchase_order_item.id;


--
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles (
    id bigint NOT NULL,
    description character varying(255),
    name character varying(255)
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- Name: roles_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.roles_id_seq OWNER TO postgres;

--
-- Name: roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.roles_id_seq OWNED BY public.roles.id;


--
-- Name: sales_invoice; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sales_invoice (
    id bigint NOT NULL,
    code character varying(50) NOT NULL,
    created_at timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    discount_amount numeric(15,2),
    due_date date,
    invoice_date date NOT NULL,
    issued_by bigint,
    issued_date timestamp(6) without time zone,
    notes character varying(500),
    paid_amount numeric(15,2),
    paid_date timestamp(6) without time zone,
    payment_method character varying(50),
    payment_reference character varying(100),
    remaining_amount numeric(15,2),
    shipping_cost numeric(15,2),
    status character varying(30) NOT NULL,
    subtotal numeric(15,2),
    tax_amount numeric(15,2),
    total_amount numeric(15,2),
    updated_at timestamp(6) without time zone,
    customer_id bigint NOT NULL,
    goods_issue_id bigint NOT NULL,
    sales_order_id bigint NOT NULL,
    CONSTRAINT sales_invoice_status_check CHECK (((status)::text = ANY (ARRAY[('DRAFT'::character varying)::text, ('ISSUED'::character varying)::text, ('PARTIALLY_PAID'::character varying)::text, ('PAID'::character varying)::text, ('CANCELLED'::character varying)::text, ('OVERDUE'::character varying)::text])))
);


ALTER TABLE public.sales_invoice OWNER TO postgres;

--
-- Name: sales_invoice_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sales_invoice_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sales_invoice_id_seq OWNER TO postgres;

--
-- Name: sales_invoice_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sales_invoice_id_seq OWNED BY public.sales_invoice.id;


--
-- Name: sales_invoice_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sales_invoice_item (
    id bigint NOT NULL,
    amount_before_tax numeric(15,2),
    description character varying(500),
    discount_percent numeric(5,2),
    quantity integer NOT NULL,
    tax_amount numeric(15,2),
    tax_percent numeric(5,2),
    total_amount numeric(15,2),
    unit character varying(50),
    unit_price numeric(15,2),
    goods_issue_item_id bigint,
    product_id bigint NOT NULL,
    sales_invoice_id bigint NOT NULL
);


ALTER TABLE public.sales_invoice_item OWNER TO postgres;

--
-- Name: sales_invoice_item_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sales_invoice_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sales_invoice_item_id_seq OWNER TO postgres;

--
-- Name: sales_invoice_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sales_invoice_item_id_seq OWNED BY public.sales_invoice_item.id;


--
-- Name: sales_order; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sales_order (
    id bigint NOT NULL,
    approved_by bigint,
    approved_date timestamp(6) without time zone,
    code character varying(50) NOT NULL,
    completed_date timestamp(6) without time zone,
    created_at timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by bigint,
    discount_amount numeric(15,2),
    expected_delivery_date date,
    grand_total numeric(15,2),
    notes character varying(500),
    order_date date NOT NULL,
    order_name character varying(255),
    payment_status character varying(50),
    rejection_reason character varying(500),
    shipping_cost numeric(15,2),
    status character varying(50) NOT NULL,
    tax_amount numeric(15,2),
    total_amount numeric(15,2),
    updated_at timestamp(6) without time zone,
    customer_id bigint NOT NULL,
    delivery_address_id bigint,
    warehouse_id bigint,
    CONSTRAINT sales_order_payment_status_check CHECK (((payment_status)::text = ANY (ARRAY[('UNPAID'::character varying)::text, ('PARTIALLY_PAID'::character varying)::text, ('PAID'::character varying)::text, ('REFUNDED'::character varying)::text]))),
    CONSTRAINT sales_order_status_check CHECK (((status)::text = ANY (ARRAY[('ORDER_OPEN'::character varying)::text, ('ORDER_APPROVED'::character varying)::text, ('ORDER_PARTIALLY_DELIVERED'::character varying)::text, ('ORDER_COMPLETED'::character varying)::text, ('ORDER_CANCELLED'::character varying)::text])))
);


ALTER TABLE public.sales_order OWNER TO postgres;

--
-- Name: sales_order_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sales_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sales_order_id_seq OWNER TO postgres;

--
-- Name: sales_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sales_order_id_seq OWNED BY public.sales_order.id;


--
-- Name: sales_order_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sales_order_item (
    id bigint NOT NULL,
    amount_before_tax numeric(15,2),
    delivered_quantity integer,
    discount_percent numeric(5,2),
    notes character varying(500),
    quantity integer NOT NULL,
    tax_amount numeric(15,2),
    tax_percent numeric(5,2),
    total_amount numeric(15,2),
    unit character varying(50),
    unit_price numeric(15,2) NOT NULL,
    product_id bigint NOT NULL,
    sales_order_id bigint NOT NULL
);


ALTER TABLE public.sales_order_item OWNER TO postgres;

--
-- Name: sales_order_item_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sales_order_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sales_order_item_id_seq OWNER TO postgres;

--
-- Name: sales_order_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sales_order_item_id_seq OWNED BY public.sales_order_item.id;


--
-- Name: supplier; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.supplier (
    id bigint NOT NULL,
    address character varying(255),
    code character varying(255) NOT NULL,
    contact_name character varying(255),
    email character varying(255),
    name character varying(255) NOT NULL,
    phone character varying(255)
);


ALTER TABLE public.supplier OWNER TO postgres;

--
-- Name: supplier_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.supplier_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.supplier_id_seq OWNER TO postgres;

--
-- Name: supplier_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.supplier_id_seq OWNED BY public.supplier.id;


--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_roles (
    user_id bigint NOT NULL,
    role_id bigint NOT NULL
);


ALTER TABLE public.user_roles OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    active boolean NOT NULL,
    email character varying(255),
    full_name character varying(255),
    password character varying(255),
    username character varying(255) NOT NULL
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: warehouse; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.warehouse (
    id bigint NOT NULL,
    code character varying(255) NOT NULL,
    description character varying(255),
    location character varying(255),
    name character varying(255) NOT NULL
);


ALTER TABLE public.warehouse OWNER TO postgres;

--
-- Name: warehouse_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.warehouse_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.warehouse_id_seq OWNER TO postgres;

--
-- Name: warehouse_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.warehouse_id_seq OWNED BY public.warehouse.id;


--
-- Name: customer id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer ALTER COLUMN id SET DEFAULT nextval('public.customer_id_seq'::regclass);


--
-- Name: delivery_address id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_address ALTER COLUMN id SET DEFAULT nextval('public.delivery_address_id_seq'::regclass);


--
-- Name: delivery_order id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_order ALTER COLUMN id SET DEFAULT nextval('public.delivery_order_id_seq'::regclass);


--
-- Name: delivery_plan id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_plan ALTER COLUMN id SET DEFAULT nextval('public.delivery_plan_id_seq'::regclass);


--
-- Name: delivery_plan_order id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_plan_order ALTER COLUMN id SET DEFAULT nextval('public.delivery_plan_order_id_seq'::regclass);


--
-- Name: delivery_plan_shipper id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_plan_shipper ALTER COLUMN id SET DEFAULT nextval('public.delivery_plan_shipper_id_seq'::regclass);


--
-- Name: delivery_triproute id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_triproute ALTER COLUMN id SET DEFAULT nextval('public.delivery_triproute_id_seq'::regclass);


--
-- Name: delivery_triproute_item id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_triproute_item ALTER COLUMN id SET DEFAULT nextval('public.delivery_triproute_item_id_seq'::regclass);


--
-- Name: goods_issue id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_issue ALTER COLUMN id SET DEFAULT nextval('public.goods_issue_id_seq'::regclass);


--
-- Name: goods_issue_item id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_issue_item ALTER COLUMN id SET DEFAULT nextval('public.goods_issue_item_id_seq'::regclass);


--
-- Name: goods_receipt id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_receipt ALTER COLUMN id SET DEFAULT nextval('public.goods_receipt_id_seq'::regclass);


--
-- Name: goods_receipt_item id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_receipt_item ALTER COLUMN id SET DEFAULT nextval('public.goods_receipt_item_id_seq'::regclass);


--
-- Name: inventory id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory ALTER COLUMN id SET DEFAULT nextval('public.inventory_id_seq'::regclass);


--
-- Name: inventory_lot id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_lot ALTER COLUMN id SET DEFAULT nextval('public.inventory_lot_id_seq'::regclass);


--
-- Name: inventory_reservation id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_reservation ALTER COLUMN id SET DEFAULT nextval('public.inventory_reservation_id_seq'::regclass);


--
-- Name: inventory_transaction id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_transaction ALTER COLUMN id SET DEFAULT nextval('public.inventory_transaction_id_seq'::regclass);


--
-- Name: invoice id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice ALTER COLUMN id SET DEFAULT nextval('public.invoice_id_seq'::regclass);


--
-- Name: product id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product ALTER COLUMN id SET DEFAULT nextval('public.product_id_seq'::regclass);


--
-- Name: purchase_order id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.purchase_order ALTER COLUMN id SET DEFAULT nextval('public.purchase_order_id_seq'::regclass);


--
-- Name: purchase_order_item id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.purchase_order_item ALTER COLUMN id SET DEFAULT nextval('public.purchase_order_item_id_seq'::regclass);


--
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles ALTER COLUMN id SET DEFAULT nextval('public.roles_id_seq'::regclass);


--
-- Name: sales_invoice id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_invoice ALTER COLUMN id SET DEFAULT nextval('public.sales_invoice_id_seq'::regclass);


--
-- Name: sales_invoice_item id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_invoice_item ALTER COLUMN id SET DEFAULT nextval('public.sales_invoice_item_id_seq'::regclass);


--
-- Name: sales_order id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_order ALTER COLUMN id SET DEFAULT nextval('public.sales_order_id_seq'::regclass);


--
-- Name: sales_order_item id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_order_item ALTER COLUMN id SET DEFAULT nextval('public.sales_order_item_id_seq'::regclass);


--
-- Name: supplier id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.supplier ALTER COLUMN id SET DEFAULT nextval('public.supplier_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: warehouse id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.warehouse ALTER COLUMN id SET DEFAULT nextval('public.warehouse_id_seq'::regclass);


--
-- Data for Name: customer; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.customer (id, active, code, contact_name, created_at, credit_limit, current_balance, email, name, payment_terms, phone, tax_code, updated_at) FROM stdin;
1	t	CUST001	Trần Hữu Phúc	\N	500000000.00	\N	phuc.tran@ctmbd.vn	Công ty CP Chế Tạo Máy Bình Dương	30	0274-395-1111	3701234567	\N
2	t	CUST002	Lê Thị Mỹ Hạnh	\N	300000000.00	\N	hanh.le@donganhua.vn	Công ty TNHH SX Nhựa Kỹ Thuật Đông Á	45	028-3755-2222	0312345678	\N
3	t	CUST003	Nguyễn Công Trí	\N	800000000.00	\N	tri.nguyen@xaydung1.vn	Tổng Công ty Xây Dựng Số 1 - TNHH MTV	60	024-3566-3333	0100234567	\N
4	t	CUST004	Phạm Ngọc Hân	\N	400000000.00	\N	han.pham@saigonfood.vn	Công ty CP Thực Phẩm Sài Gòn Food	30	028-3844-4444	0300345678	\N
5	t	CUST005	Kim Jae-won	\N	2000000000.00	\N	jaewon.kim@samsungvina.vn	Công ty TNHH Điện Tử Samsung Vina	45	0222-396-5555	2300456789	\N
6	t	CUST006	Vũ Đình Khoa	\N	250000000.00	\N	khoa.vu@moitruongxanh.vn	Công ty CP Nước Sạch Môi Trường Xanh	30	028-3910-6666	0303567890	\N
7	t	CUST007	Đỗ Văn Long	\N	350000000.00	\N	long.do@tanthanh.vn	Công ty TNHH Cơ Khí Chính Xác Tân Thành	45	0274-382-7777	3700678901	\N
8	t	CUST008	Trương Thị Mai	\N	600000000.00	\N	mai.truong@imexpharm.vn	Công ty CP Dược Phẩm Imexpharm	60	0292-389-8888	1800789012	\N
9	t	CUST009	Nguyễn Thanh Nam	\N	700000000.00	\N	nam.nguyen@viettien.vn	Công ty TNHH Dệt May Việt Tiến	30	028-3864-9999	0301890123	\N
10	t	CUST010	Hoàng Phi Yến	\N	450000000.00	\N	yen.hoang@giaybaibang.vn	Công ty CP Giấy Bãi Bằng	45	0210-388-0101	2600901234	\N
11	t	CUST011	Bùi Ngọc Anh	\N	1500000000.00	\N	anh.bui@thaco.vn	Công ty TNHH Ô Tô Trường Hải - THACO	60	0235-385-0202	5100012345	\N
12	t	CUST012	Đinh Quốc Bảo	\N	900000000.00	\N	bao.dinh@hatien2.vn	Công ty CP Xi Măng Hà Tiên 2	45	028-3751-0303	0303123456	\N
13	t	CUST013	Lý Thiên Cương	\N	1200000000.00	\N	cuong.ly@hoaphat.vn	Công ty TNHH Thép Hòa Phát Miền Nam	30	028-3613-0404	0312234567	\N
14	t	CUST014	Phan Thị Diễm	\N	200000000.00	\N	diem.phan@nhuadanang.vn	Công ty CP Nhựa Đà Nẵng	30	0236-395-0505	0400345678	\N
15	t	CUST015	Mai Thanh Gia	\N	600000000.00	\N	gia.mai@longhau.vn	Công ty TNHH KCN Long Hậu	45	028-3718-0606	0311456789	\N
\.


--
-- Data for Name: delivery_address; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.delivery_address (id, address_line1, address_line2, address_name, city, country, created_at, is_default, notes, phone, postal_code, recipient_name, state, updated_at, customer_id) FROM stdin;
1	100 Bình Dương Avenue	\N	Trụ sở chính	Bình Dương	Vietnam	\N	t	\N	0274-395-1111	75000	Trần Hữu Phúc	Bình Dương	\N	1
2	Lô B12 KCN Sóng Thần 2	\N	Nhà máy KCN Sóng Thần	Bình Dương	Vietnam	\N	f	\N	0274-395-1112	75000	Quản lý kho	Bình Dương	\N	1
3	45 Đinh Bộ Lĩnh, Q.Bình Thạnh	\N	Văn phòng chính	TP.HCM	Vietnam	\N	t	\N	028-3755-2222	70000	Lê Thị Mỹ Hạnh	TP.HCM	\N	2
4	37 Lê Đại Hành, Hai Bà Trưng	\N	Ban điều hành	Hà Nội	Vietnam	\N	t	\N	024-3566-3333	10000	Nguyễn Công Trí	Hà Nội	\N	3
5	Khu đô thị Ecopark, Hưng Yên	\N	Công trình Ecopark	Hưng Yên	Vietnam	\N	f	\N	024-3566-3334	17000	Chỉ huy trưởng	Hưng Yên	\N	3
6	Lô 23 KCN Lê Minh Xuân, Bình Chánh	\N	Nhà máy Bình Chánh	TP.HCM	Vietnam	\N	t	\N	028-3844-4444	70000	Phạm Ngọc Hân	TP.HCM	\N	4
7	KCN Yên Phong, Bắc Ninh	\N	Samsung Complex	Bắc Ninh	Vietnam	\N	t	\N	0222-396-5555	16000	Kim Jae-won	Bắc Ninh	\N	5
8	233 Nguyễn Trãi, Q.Thanh Xuân	\N	Nhà máy xử lý nước	Hà Nội	Vietnam	\N	t	\N	028-3910-6666	10000	Vũ Đình Khoa	Hà Nội	\N	6
9	Lô CN-07 KCN Đồng An, Bình Dương	\N	Xưởng sản xuất	Bình Dương	Vietnam	\N	t	\N	0274-382-7777	75000	Đỗ Văn Long	Bình Dương	\N	7
10	04 Nguyễn Thị Minh Khai, Sa Đéc	\N	Nhà máy sản xuất	Đồng Tháp	Vietnam	\N	t	\N	0292-389-8888	81000	Trương Thị Mai	Đồng Tháp	\N	8
11	Lô B3 KCN Tân Bình, TP.HCM	\N	Xưởng may chính	TP.HCM	Vietnam	\N	t	\N	028-3864-9999	70000	Nguyễn Thanh Nam	TP.HCM	\N	9
12	KCN Phú Thọ, Phú Thọ	\N	Nhà máy giấy	Phú Thọ	Vietnam	\N	t	\N	0210-388-0101	29000	Hoàng Phi Yến	Phú Thọ	\N	10
13	KKT mở Chu Lai, Núi Thành	\N	Tổ hợp THACO Chu Lai	Quảng Nam	Vietnam	\N	t	\N	0235-385-0202	51000	Bùi Ngọc Anh	Quảng Nam	\N	11
14	Lô A5, KCN Hiệp Phước, Nhà Bè	\N	Nhà máy xi măng	TP.HCM	Vietnam	\N	t	\N	028-3751-0303	70000	Đinh Quốc Bảo	TP.HCM	\N	12
15	288 Lý Thường Kiệt, Q.10, TP.HCM	\N	Văn phòng & Kho HCM	TP.HCM	Vietnam	\N	t	\N	028-3613-0404	70000	Lý Thiên Cương	TP.HCM	\N	13
16	Lô D7, KCN Hòa Khánh, Liên Chiểu	\N	Nhà máy Đà Nẵng	Đà Nẵng	Vietnam	\N	t	\N	0236-395-0505	55000	Phan Thị Diễm	Đà Nẵng	\N	14
17	KCN Long Hậu, Long An	\N	KCN Long Hậu văn phòng	Long An	Vietnam	\N	t	\N	028-3718-0606	82000	Mai Thanh Gia	Long An	\N	15
18	123 Main Street	\N	Main Office	Ho Chi Minh City	\N	\N	t	\N	0274-395-1111	70000	Trần Hữu Phúc	HCMC	\N	1
19	456 Industrial Zone	\N	Warehouse	Binh Duong	\N	\N	f	\N	111-222-3334	75000	Warehouse Manager	BD	\N	1
20	789 Tech Park	\N	Head Office	Ha Noi	\N	\N	t	\N	028-3755-2222	10000	Lê Thị Mỹ Hạnh	HN	\N	2
\.


--
-- Data for Name: delivery_order; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.delivery_order (id, code, destination_address, status, sales_order_id) FROM stdin;
1	GI-1777631788989	123 Main Street, Ho Chi Minh City, HCMC 70000	Pending	\N
2	GI-2026-021	KCN Yên Phong, Bắc Ninh, Bắc Ninh, Bắc Ninh 16000, Vietnam	Pending	\N
3	GI-2026-020	Lô D7, KCN Hòa Khánh, Liên Chiểu, Đà Nẵng, Đà Nẵng 55000, Vietnam	Pending	\N
4	GI-2026-019	Lô A5, KCN Hiệp Phước, Nhà Bè, TP.HCM, TP.HCM 70000, Vietnam	Pending	\N
5	GI-2026-018	100 Bình Dương Avenue, Bình Dương, Bình Dương 75000, Vietnam	Pending	\N
6	GI-2026-017	Lô 23 KCN Lê Minh Xuân, Bình Chánh, TP.HCM, TP.HCM 70000, Vietnam	Pending	\N
7	GI-2026-016	Lô CN-07 KCN Đồng An, Bình Dương, Bình Dương, Bình Dương 75000, Vietnam	Pending	\N
8	GI-2026-015	233 Nguyễn Trãi, Q.Thanh Xuân, Hà Nội, Hà Nội 10000, Vietnam	Pending	\N
9	GI-2026-014	04 Nguyễn Thị Minh Khai, Sa Đéc, Đồng Tháp, Đồng Tháp 81000, Vietnam	Pending	\N
10	GI-2026-013	288 Lý Thường Kiệt, Q.10, TP.HCM, TP.HCM, TP.HCM 70000, Vietnam	Pending	\N
11	GI-2026-012	37 Lê Đại Hành, Hai Bà Trưng, Hà Nội, Hà Nội 10000, Vietnam	Pending	\N
12	GI-2026-011	KCN Yên Phong, Bắc Ninh, Bắc Ninh, Bắc Ninh 16000, Vietnam	Pending	\N
13	GI-2026-010	04 Nguyễn Thị Minh Khai, Sa Đéc, Đồng Tháp, Đồng Tháp 81000, Vietnam	Pending	\N
14	GI-2026-009	Lô B3 KCN Tân Bình, TP.HCM, TP.HCM, TP.HCM 70000, Vietnam	Pending	\N
15	GI-2026-008	288 Lý Thường Kiệt, Q.10, TP.HCM, TP.HCM, TP.HCM 70000, Vietnam	Pending	\N
16	GI-2026-007	KKT mở Chu Lai, Núi Thành, Quảng Nam, Quảng Nam 51000, Vietnam	Pending	\N
17	GI-2026-006	37 Lê Đại Hành, Hai Bà Trưng, Hà Nội, Hà Nội 10000, Vietnam	Pending	\N
18	GI-2026-005	233 Nguyễn Trãi, Q.Thanh Xuân, Hà Nội, Hà Nội 10000, Vietnam	Pending	\N
19	GI-2026-004	Lô 23 KCN Lê Minh Xuân, Bình Chánh, TP.HCM, TP.HCM 70000, Vietnam	Pending	\N
20	GI-2026-003	Lô CN-07 KCN Đồng An, Bình Dương, Bình Dương, Bình Dương 75000, Vietnam	Pending	\N
21	GI-2026-002	KCN Yên Phong, Bắc Ninh, Bắc Ninh, Bắc Ninh 16000, Vietnam	Pending	\N
22	GI-2026-001	100 Bình Dương Avenue, Bình Dương, Bình Dương 75000, Vietnam	Pending	\N
\.


--
-- Data for Name: delivery_plan; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.delivery_plan (id, code, created_date, description, status) FROM stdin;
5	\N	2026-05-30	\N	Completed
6	\N	2026-05-30	\N	Created
\.


--
-- Data for Name: delivery_plan_order; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.delivery_plan_order (id, delivery_order_id, delivery_plan_id) FROM stdin;
11	1	5
12	2	5
13	3	5
14	4	5
15	18	6
16	17	6
17	16	6
\.


--
-- Data for Name: delivery_plan_shipper; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.delivery_plan_shipper (id, phone, shipper_name, delivery_plan_id) FROM stdin;
8		Shipper One	5
9		Shipper Two	5
\.


--
-- Data for Name: delivery_triproute; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.delivery_triproute (id, code, completed_at, notes, shipper_name, started_at, status, delivery_plan_id, shipper_user_id) FROM stdin;
6	TRIP-1780120840531-13	2026-05-30 13:04:55.682971	\N	Shipper One	2026-05-30 13:04:54.433431	COMPLETED	5	13
7	TRIP-1780120840548-14	2026-05-30 13:05:15.964744	\N	Shipper Two	2026-05-30 13:05:15.062046	COMPLETED	5	14
\.


--
-- Data for Name: delivery_triproute_item; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.delivery_triproute_item (id, sequence, status, delivery_order_id, triproute_id) FROM stdin;
15	1	Delivered	1	6
16	2	Delivered	3	6
17	1	Delivered	2	7
18	2	Delivered	4	7
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	0	<< Flyway Baseline >>	BASELINE	<< Flyway Baseline >>	\N	postgres	2026-03-28 18:27:20.030648	0	t
2	1	purchasing module	SQL	V1__purchasing_module.sql	-1958158426	postgres	2026-03-28 18:27:20.06825	14	t
3	2	sales module	SQL	V2__sales_module.sql	-1070775231	postgres	2026-03-28 18:27:20.092346	13	t
4	3	security and rbac	SQL	V3__security_and_rbac.sql	-342959977	postgres	2026-03-28 18:27:20.110692	13	t
\.


--
-- Data for Name: goods_issue; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.goods_issue (id, carrier_name, code, confirmed_by, confirmed_date, created_at, created_by, delivery_note_number, issue_date, notes, shipping_method, status, total_amount, tracking_number, updated_at, delivery_address_id, sales_order_id, warehouse_id) FROM stdin;
1	Giao Hàng Nhanh	GI-2026-001	10	2026-01-22 15:00:00	2026-03-28 18:26:55.175667	11	GI-DN-001	2026-01-20	\N	Xe tải	CONFIRMED	94050000.00	\N	\N	1	1	1
2	Giao Hàng Nhanh	GI-2026-002	10	2026-01-23 16:00:00	2026-03-28 18:26:55.175667	11	GI-DN-002	2026-01-21	\N	Xe tải	CONFIRMED	198000000.00	\N	\N	7	2	1
3	Giao Hàng Nhanh	GI-2026-003	10	2026-01-26 14:00:00	2026-03-28 18:26:55.175667	11	GI-DN-003	2026-01-24	\N	Xe tải	CONFIRMED	159500000.00	\N	\N	9	3	1
4	Giao Hàng Nhanh	GI-2026-004	10	2026-01-27 16:00:00	2026-03-28 18:26:55.175667	11	GI-DN-004	2026-01-25	\N	Xe tải	CONFIRMED	101200000.00	\N	\N	6	4	1
5	Giao Hàng Nhanh	GI-2026-005	10	2026-01-28 17:00:00	2026-03-28 18:26:55.175667	11	GI-DN-005	2026-01-26	\N	Xe tải	CONFIRMED	162800000.00	\N	\N	8	5	2
6	Giao Hàng Nhanh	GI-2026-006	10	2026-01-29 16:00:00	2026-03-28 18:26:55.175667	11	GI-DN-006	2026-01-27	\N	Xe tải	CONFIRMED	181500000.00	\N	\N	4	6	1
7	Giao Hàng Nhanh	GI-2026-007	10	2026-01-30 15:00:00	2026-03-28 18:26:55.175667	11	GI-DN-007	2026-01-28	\N	Xe tải	CONFIRMED	107800000.00	\N	\N	13	7	1
8	Giao Hàng Nhanh	GI-2026-008	10	2026-01-31 17:00:00	2026-03-28 18:26:55.175667	11	GI-DN-008	2026-01-29	\N	Xe tải	CONFIRMED	242000000.00	\N	\N	15	8	1
9	Giao Hàng Nhanh	GI-2026-009	10	2026-02-02 15:00:00	2026-03-28 18:26:55.175667	11	GI-DN-009	2026-01-31	\N	Xe tải	CONFIRMED	57200000.00	\N	\N	11	9	2
10	Giao Hàng Nhanh	GI-2026-010	10	2026-02-04 16:00:00	2026-03-28 18:26:55.175667	11	GI-DN-010	2026-02-02	\N	Xe tải	CONFIRMED	85800000.00	\N	\N	10	10	1
11	Giao Hàng Nhanh	GI-2026-011	10	2026-02-21 16:00:00	2026-03-28 18:26:55.175667	11	GI-DN-011	2026-02-19	\N	Xe tải	CONFIRMED	132000000.00	\N	\N	7	11	1
12	Giao Hàng Nhanh	GI-2026-012	10	2026-02-24 14:00:00	2026-03-28 18:26:55.175667	11	GI-DN-012	2026-02-22	\N	Xe tải	CONFIRMED	217800000.00	\N	\N	4	12	1
13	Giao Hàng Nhanh	GI-2026-013	10	2026-02-26 15:00:00	2026-03-28 18:26:55.175667	11	GI-DN-013	2026-02-24	\N	Xe tải	CONFIRMED	290400000.00	\N	\N	15	13	1
14	Giao Hàng Nhanh	GI-2026-014	10	2026-02-27 16:00:00	2026-03-28 18:26:55.175667	11	GI-DN-014	2026-02-25	\N	Xe tải	CONFIRMED	104500000.00	\N	\N	10	14	1
15	Giao Hàng Nhanh	GI-2026-015	10	2026-03-01 15:00:00	2026-03-28 18:26:55.175667	11	GI-DN-015	2026-02-27	\N	Xe tải	CONFIRMED	82500000.00	\N	\N	8	15	1
16	Giao Hàng Nhanh	GI-2026-016	10	2026-03-02 14:00:00	2026-03-28 18:26:55.175667	11	GI-DN-016	2026-02-28	\N	Xe tải	CONFIRMED	143000000.00	\N	\N	9	16	1
17	Giao Hàng Nhanh	GI-2026-017	10	2026-03-03 16:00:00	2026-03-28 18:26:55.175667	11	GI-DN-017	2026-03-01	\N	Xe tải	CONFIRMED	121000000.00	\N	\N	6	17	2
18	Giao Hàng Nhanh	GI-2026-018	10	2026-03-05 15:00:00	2026-03-28 18:26:55.175667	11	GI-DN-018	2026-03-03	\N	Xe tải	CONFIRMED	96800000.00	\N	\N	1	18	1
19	Giao Hàng Nhanh	GI-2026-019	10	2026-03-07 16:00:00	2026-03-28 18:26:55.175667	11	GI-DN-019	2026-03-05	\N	Xe tải	CONFIRMED	247500000.00	\N	\N	14	19	2
20	Giao Hàng Nhanh	GI-2026-020	10	2026-03-09 14:00:00	2026-03-28 18:26:55.175667	11	GI-DN-020	2026-03-07	\N	Xe tải	CONFIRMED	71500000.00	\N	\N	16	20	2
21	Giao Hàng Nhanh	GI-2026-021	10	2026-03-21 16:00:00	2026-03-28 18:26:55.175667	11	GI-DN-021	2026-03-19	\N	Xe tải	CONFIRMED	220000000.00	\N	\N	7	21	1
22		GI-1777631788989	\N	2026-05-01 17:36:34.724528	2026-05-01 17:36:29.005605	\N		2026-05-01			CONFIRMED	0.00		2026-05-01 17:36:34.734444	18	38	1
23		GI-1780127528771	\N	\N	2026-05-30 14:52:08.779117	\N		2026-05-30			DRAFT	0.00		2026-05-30 14:52:08.779138	10	40	1
24		GI-1780127548593	\N	\N	2026-05-30 14:52:28.598677	\N		2026-05-30			DRAFT	0.00		2026-05-30 14:52:28.598691	10	40	1
\.


--
-- Data for Name: goods_issue_item; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.goods_issue_item (id, batch_number, expiry_date, issued_quantity, notes, ordered_quantity, total_amount, unit, unit_price, goods_issue_id, product_id, sales_order_item_id) FROM stdin;
1	BATCH-ISSUE-001-1	\N	3	\N	3	31350000.00	Cái	9500000.00	1	1	1
2	BATCH-ISSUE-001-2	\N	30	\N	30	16500000.00	Cái	500000.00	1	2	2
3	BATCH-ISSUE-001-23	\N	8	\N	8	11880000.00	Cái	1350000.00	1	23	3
4	BATCH-ISSUE-001-24	\N	20	\N	20	9240000.00	Cái	420000.00	1	24	4
5	BATCH-ISSUE-002-1	\N	5	\N	5	52250000.00	Cái	9500000.00	2	1	5
6	BATCH-ISSUE-002-3	\N	3	\N	3	44550000.00	Cái	13500000.00	2	3	6
7	BATCH-ISSUE-002-2	\N	50	\N	50	27500000.00	Cái	500000.00	2	2	7
8	BATCH-ISSUE-003-12	\N	200	\N	200	70400000.00	Cây	320000.00	3	12	8
9	BATCH-ISSUE-003-13	\N	25	\N	25	57750000.00	Tấm	2100000.00	3	13	9
10	BATCH-ISSUE-003-14	\N	20	\N	20	24200000.00	Cây	1100000.00	3	14	10
11	BATCH-ISSUE-004-8	\N	100	\N	100	34100000.00	Bao	310000.00	4	8	11
12	BATCH-ISSUE-004-10	\N	50	\N	50	39600000.00	Cái	720000.00	4	10	12
13	BATCH-ISSUE-004-9	\N	50	\N	50	21450000.00	Can	390000.00	4	9	13
14	BATCH-ISSUE-005-6	\N	2	\N	2	110000000.00	Cái	50000000.00	5	6	14
15	BATCH-ISSUE-005-7	\N	60	\N	60	23760000.00	Cái	360000.00	5	7	15
16	BATCH-ISSUE-005-25	\N	15	\N	15	5940000.00	Cái	360000.00	5	25	16
17	BATCH-ISSUE-006-4	\N	800	\N	800	92400000.00	Bao	105000.00	6	4	17
18	BATCH-ISSUE-006-5	\N	400	\N	400	88000000.00	Tấm	200000.00	6	5	18
19	BATCH-ISSUE-007-20	\N	200	\N	200	20900000.00	Cái	95000.00	7	20	19
20	BATCH-ISSUE-007-18	\N	150	\N	150	17325000.00	Cái	105000.00	7	18	20
21	BATCH-ISSUE-007-12	\N	120	\N	120	42240000.00	Cây	320000.00	7	12	21
22	BATCH-ISSUE-007-13	\N	10	\N	10	23100000.00	Tấm	2100000.00	7	13	22
23	BATCH-ISSUE-008-12	\N	400	\N	400	140800000.00	Cây	320000.00	8	12	23
24	BATCH-ISSUE-008-13	\N	40	\N	40	92400000.00	Tấm	2100000.00	8	13	24
25	BATCH-ISSUE-009-18	\N	150	\N	150	17325000.00	Cái	105000.00	9	18	25
26	BATCH-ISSUE-009-20	\N	100	\N	100	10450000.00	Cái	95000.00	9	20	26
27	BATCH-ISSUE-009-24	\N	30	\N	30	13860000.00	Cái	420000.00	9	24	27
28	BATCH-ISSUE-009-25	\N	10	\N	10	3960000.00	Cái	360000.00	9	25	28
29	BATCH-ISSUE-010-16	\N	2	\N	2	45100000.00	Cuộn	20500000.00	10	16	29
30	BATCH-ISSUE-010-17	\N	25	\N	25	26125000.00	Cái	950000.00	10	17	30
31	BATCH-ISSUE-010-25	\N	8	\N	8	3168000.00	Cái	360000.00	10	25	31
32	BATCH-ISSUE-011-1	\N	4	\N	4	41800000.00	Cái	9500000.00	11	1	32
33	BATCH-ISSUE-011-3	\N	2	\N	2	29700000.00	Cái	13500000.00	11	3	33
34	BATCH-ISSUE-011-23	\N	10	\N	10	14850000.00	Cái	1350000.00	11	23	34
35	BATCH-ISSUE-012-4	\N	1000	\N	1000	115500000.00	Bao	105000.00	12	4	35
36	BATCH-ISSUE-012-5	\N	500	\N	500	110000000.00	Tấm	200000.00	12	5	36
37	BATCH-ISSUE-013-12	\N	450	\N	450	158400000.00	Cây	320000.00	13	12	37
38	BATCH-ISSUE-013-13	\N	50	\N	50	115500000.00	Tấm	2100000.00	13	13	38
39	BATCH-ISSUE-014-16	\N	3	\N	3	67650000.00	Cuộn	20500000.00	14	16	39
40	BATCH-ISSUE-014-17	\N	30	\N	30	31350000.00	Cái	950000.00	14	17	40
41	BATCH-ISSUE-015-8	\N	120	\N	120	40920000.00	Bao	310000.00	15	8	41
42	BATCH-ISSUE-015-9	\N	80	\N	80	34320000.00	Can	390000.00	15	9	42
43	BATCH-ISSUE-016-14	\N	60	\N	60	72600000.00	Cây	1100000.00	16	14	43
44	BATCH-ISSUE-016-2	\N	25	\N	25	68750000.00	Tấm	2500000.00	16	2	44
45	BATCH-ISSUE-017-10	\N	80	\N	80	63360000.00	Cái	720000.00	17	10	45
46	BATCH-ISSUE-017-7	\N	60	\N	60	23760000.00	Cái	360000.00	17	7	46
47	BATCH-ISSUE-018-20	\N	300	\N	300	31350000.00	Cái	95000.00	18	20	47
48	BATCH-ISSUE-018-18	\N	200	\N	200	23100000.00	Cái	105000.00	18	18	48
49	BATCH-ISSUE-018-24	\N	30	\N	30	13860000.00	Cái	420000.00	18	24	49
50	BATCH-ISSUE-018-25	\N	10	\N	10	3960000.00	Cái	360000.00	18	25	50
51	BATCH-ISSUE-019-6	\N	3	\N	3	165000000.00	Cái	50000000.00	19	6	51
52	BATCH-ISSUE-019-7	\N	50	\N	50	19800000.00	Cái	360000.00	19	7	52
53	BATCH-ISSUE-019-25	\N	10	\N	10	3960000.00	Cái	360000.00	19	25	53
54	BATCH-ISSUE-020-10	\N	50	\N	50	39600000.00	Cái	720000.00	20	10	54
55	BATCH-ISSUE-020-8	\N	60	\N	60	20460000.00	Bao	310000.00	20	8	55
56	BATCH-ISSUE-021-1	\N	6	\N	6	62700000.00	Cái	9500000.00	21	1	56
57	BATCH-ISSUE-021-3	\N	4	\N	4	59400000.00	Cái	13500000.00	21	3	57
58	BATCH-ISSUE-021-2	\N	60	\N	60	33000000.00	Cái	500000.00	21	2	58
59	L002	2026-05-02	7		7	59500000.00	\N	8500000.00	22	1	88
60		\N	12		12	102000000.00	\N	8500000.00	23	1	92
61		\N	12		12	102000000.00	\N	8500000.00	24	1	92
\.


--
-- Data for Name: goods_receipt; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.goods_receipt (id, code, confirmed_by, confirmed_date, created_at, created_by, delivery_note_number, invoice_number, notes, receipt_date, status, total_amount, updated_at, purchase_order_id, warehouse_id) FROM stdin;
1	GR-2026-001	8	2026-01-15 16:30:00	2026-03-28 18:26:55.162527	11	DN-SUP001-2601	\N	Nhận đủ hàng, kiểm tra OK	2026-01-15	CONFIRMED	123640000.00	\N	1	1
2	GR-2026-002	8	2026-01-20 14:00:00	2026-03-28 18:26:55.162527	11	DN-SUP002-2601	\N	Giao 2 chuyến, nhận đủ	2026-01-20	CONFIRMED	203500000.00	\N	2	1
3	GR-2026-003	8	2026-01-25 11:00:00	2026-03-28 18:26:55.162527	11	DN-SUP003-2601	\N	Kiểm tra chất lượng trước khi nhận	2026-01-25	CONFIRMED	183700000.00	\N	3	2
4	GR-2026-004	8	2026-01-28 15:00:00	2026-03-28 18:26:55.162527	11	DN-SUP004-2601	\N	Lưu kho hóa chất đúng quy định	2026-01-28	CONFIRMED	84700000.00	\N	4	1
5	GR-2026-005	8	2026-01-30 16:00:00	2026-03-28 18:26:55.162527	11	DN-SUP005-2601	\N	\N	2026-01-30	CONFIRMED	275000000.00	\N	5	2
6	GR-2026-006	8	2026-01-31 17:00:00	2026-03-28 18:26:55.162527	11	DN-SUP006-2601	\N	Thép nhập từ Formosa, chứng nhận CQ đầy đủ	2026-01-31	CONFIRMED	276100000.00	\N	6	1
7	GR-2026-007	8	2026-02-05 14:30:00	2026-03-28 18:26:55.162527	11	DN-SUP007-2602	\N	\N	2026-02-05	CONFIRMED	158840000.00	\N	7	1
8	GR-2026-008	8	2026-02-08 16:00:00	2026-03-28 18:26:55.162527	11	DN-SUP008-2602	\N	Giao kho Hà Nội	2026-02-08	CONFIRMED	118800000.00	\N	8	3
9	GR-2026-009	8	2026-02-10 11:00:00	2026-03-28 18:26:55.162527	11	DN-SUP009-2602	\N	\N	2026-02-10	CONFIRMED	45650000.00	\N	9	2
10	GR-2026-010	8	2026-02-12 15:00:00	2026-03-28 18:26:55.162527	11	DN-SUP010-2602	\N	\N	2026-02-12	CONFIRMED	97350000.00	\N	10	1
11	GR-2026-011	8	2026-02-18 16:00:00	2026-03-28 18:26:55.162527	11	DN-SUP001-2602	\N	\N	2026-02-18	CONFIRMED	173800000.00	\N	11	1
12	GR-2026-012	8	2026-02-22 15:30:00	2026-03-28 18:26:55.162527	11	DN-SUP006-2602	\N	\N	2026-02-22	CONFIRMED	378400000.00	\N	12	1
13	GR-2026-013	8	2026-02-25 14:00:00	2026-03-28 18:26:55.162527	11	DN-SUP004-2602	\N	\N	2026-02-25	CONFIRMED	119350000.00	\N	13	1
14	GR-2026-014	8	2026-02-26 16:30:00	2026-03-28 18:26:55.162527	11	DN-SUP002-2602	\N	\N	2026-02-26	CONFIRMED	244200000.00	\N	14	2
15	GR-2026-015	8	2026-02-28 15:00:00	2026-03-28 18:26:55.162527	11	DN-SUP005-2602	\N	\N	2026-02-28	CONFIRMED	322750000.00	\N	15	2
16	GR-2026-016	8	2026-03-03 14:00:00	2026-03-28 18:26:55.162527	11	DN-SUP007-2603	\N	\N	2026-03-03	CONFIRMED	192500000.00	\N	16	1
17	GR-2026-017	8	2026-03-05 16:00:00	2026-03-28 18:26:55.162527	11	DN-SUP008-2603	\N	\N	2026-03-05	CONFIRMED	157850000.00	\N	17	3
18	GR-2026-018	8	2026-03-06 15:00:00	2026-03-28 18:26:55.162527	11	DN-SUP010-2603	\N	\N	2026-03-06	CONFIRMED	150700000.00	\N	18	1
19	GR-2026-019	8	2026-03-07 11:00:00	2026-03-28 18:26:55.162527	11	DN-SUP009-2603	\N	\N	2026-03-07	CONFIRMED	61050000.00	\N	19	2
20	GR-2026-020	8	2026-03-10 16:00:00	2026-03-28 18:26:55.162527	11	DN-SUP003-2603	\N	\N	2026-03-10	CONFIRMED	215600000.00	\N	20	2
21	GR-2026-021	8	2026-03-15 16:00:00	2026-03-28 18:26:55.162527	11	DN-SUP001-2603	\N	\N	2026-03-15	CONFIRMED	163900000.00	\N	21	1
22	GR-1774971048925	\N	2026-03-31 22:31:11.995826	2026-03-31 22:30:48.92515	\N		\N	\N	2026-04-03	CONFIRMED	0.00	2026-03-31 22:31:11.996691	27	1
23	GR-1777551516442	\N	\N	2026-04-30 19:18:36.443047	\N	\N	\N	\N	2026-04-30	DRAFT	0.00	2026-04-30 19:18:36.444623	32	1
24	GR-1777630385574	\N	2026-05-01 17:13:16.726148	2026-05-01 17:13:05.57425	\N	\N	\N	\N	2026-05-01	CONFIRMED	0.00	2026-05-01 17:13:16.727111	33	1
25	GR-1777630426491	\N	2026-05-01 17:13:48.712268	2026-05-01 17:13:46.491685	\N	\N	\N	\N	2026-05-01	CONFIRMED	0.00	2026-05-01 17:13:48.71361	34	1
26	GR-1777630624189	\N	2026-05-01 17:17:08.70675	2026-05-01 17:17:04.189585	\N	\N	\N	\N	2026-05-01	CONFIRMED	0.00	2026-05-01 17:17:08.707878	35	1
27	GR-1777630932731	\N	2026-05-01 17:22:16.236052	2026-05-01 17:22:12.731891	\N	\N	\N	\N	2026-05-01	CONFIRMED	0.00	2026-05-01 17:22:16.237287	36	1
28	GR-1780126762558	\N	\N	2026-05-30 14:39:22.558432	\N	\N	\N	\N	2026-05-30	DRAFT	0.00	2026-05-30 14:39:22.559989	38	1
\.


--
-- Data for Name: goods_receipt_item; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.goods_receipt_item (id, accepted_quantity, batch_number, expiry_date, notes, ordered_quantity, received_quantity, rejected_quantity, rejection_reason, total_amount, unit, unit_price, goods_receipt_id, product_id, purchase_order_item_id) FROM stdin;
1	5	BATCH-2601-1	\N	\N	5	5	0	\N	46750000.00	Cái	8500000.00	1	1	1
2	50	BATCH-2601-2	\N	\N	50	50	0	\N	24750000.00	Cái	450000.00	1	2	2
3	10	BATCH-2601-23	\N	\N	10	10	0	\N	13200000.00	Cái	1200000.00	1	23	3
4	30	BATCH-2601-24	\N	\N	30	30	0	\N	12540000.00	Cái	380000.00	1	24	4
5	2	BATCH-2601-3	\N	\N	2	2	0	\N	26400000.00	Cái	12000000.00	1	3	5
6	1000	BATCH-2602-4	\N	\N	1000	1000	0	\N	104500000.00	Bao	95000.00	2	4	6
7	500	BATCH-2602-5	\N	\N	500	500	0	\N	99000000.00	Tấm	180000.00	2	5	7
8	3	BATCH-2603-6	\N	\N	3	3	0	\N	148500000.00	Cái	45000000.00	3	6	8
9	80	BATCH-2603-7	\N	\N	80	80	0	\N	28160000.00	Cái	320000.00	3	7	9
10	20	BATCH-2603-25	\N	\N	20	20	0	\N	7040000.00	Cái	320000.00	3	25	10
11	150	BATCH-2604-8	\N	\N	150	150	0	\N	46200000.00	Bao	280000.00	4	8	11
12	100	BATCH-2604-9	\N	\N	100	100	0	\N	38500000.00	Can	350000.00	4	9	12
13	200	BATCH-2605-10	\N	\N	200	200	0	\N	143000000.00	Cái	650000.00	5	10	13
14	100	BATCH-2605-11	\N	\N	100	100	0	\N	132000000.00	Cái	1200000.00	5	11	14
15	500	BATCH-2606-12	\N	\N	500	500	0	\N	154000000.00	Cây	280000.00	6	12	15
16	60	BATCH-2606-13	\N	\N	60	60	0	\N	122100000.00	Tấm	1850000.00	6	13	16
17	80	BATCH-2607-14	\N	\N	80	80	0	\N	86240000.00	Cây	980000.00	7	14	17
18	30	BATCH-2607-15	\N	\N	30	30	0	\N	72600000.00	Tấm	2200000.00	7	15	18
19	4	BATCH-2608-16	\N	\N	4	4	0	\N	81400000.00	Cuộn	18500000.00	8	16	19
20	40	BATCH-2608-17	\N	\N	40	40	0	\N	37400000.00	Cái	850000.00	8	17	20
21	200	BATCH-2609-18	\N	\N	200	200	0	\N	20900000.00	Cái	95000.00	9	18	21
22	500	BATCH-2609-19	\N	\N	500	500	0	\N	24750000.00	Cái	45000.00	9	19	22
23	300	BATCH-2610-20	\N	\N	300	300	0	\N	28050000.00	Cái	85000.00	10	20	23
24	10	BATCH-2610-21	\N	\N	10	10	0	\N	49500000.00	Cái	4500000.00	10	21	24
25	100	BATCH-2610-22	\N	\N	100	100	0	\N	19800000.00	Cái	180000.00	10	22	25
26	8	BATCH-2611-1	\N	\N	8	8	0	\N	74800000.00	Cái	8500000.00	11	1	26
27	80	BATCH-2611-2	\N	\N	80	80	0	\N	39600000.00	Cái	450000.00	11	2	27
28	3	BATCH-2611-3	\N	\N	3	3	0	\N	39600000.00	Cái	12000000.00	11	3	28
29	15	BATCH-2611-23	\N	\N	15	15	0	\N	19800000.00	Cái	1200000.00	11	23	29
30	700	BATCH-2612-12	\N	\N	700	700	0	\N	215600000.00	Cây	280000.00	12	12	30
31	80	BATCH-2612-13	\N	\N	80	80	0	\N	162800000.00	Tấm	1850000.00	12	13	31
32	200	BATCH-2613-8	\N	\N	200	200	0	\N	61600000.00	Bao	280000.00	13	8	32
33	150	BATCH-2613-9	\N	\N	150	150	0	\N	57750000.00	Can	350000.00	13	9	33
34	1200	BATCH-2614-4	\N	\N	1200	1200	0	\N	125400000.00	Bao	95000.00	14	4	34
35	600	BATCH-2614-5	\N	\N	600	600	0	\N	118800000.00	Tấm	180000.00	14	5	35
36	250	BATCH-2615-10	\N	\N	250	250	0	\N	178750000.00	Cái	650000.00	15	10	36
37	120	BATCH-2615-11	\N	\N	120	120	0	\N	144000000.00	Cái	1200000.00	15	11	37
38	100	BATCH-2616-14	\N	\N	100	100	0	\N	107800000.00	Cây	980000.00	16	14	38
39	35	BATCH-2616-15	\N	\N	35	35	0	\N	84700000.00	Tấm	2200000.00	16	15	39
40	5	BATCH-2617-16	\N	\N	5	5	0	\N	101750000.00	Cuộn	18500000.00	17	16	40
41	60	BATCH-2617-17	\N	\N	60	60	0	\N	56100000.00	Cái	850000.00	17	17	41
42	500	BATCH-2618-20	\N	\N	500	500	0	\N	46750000.00	Cái	85000.00	18	20	42
43	15	BATCH-2618-21	\N	\N	15	15	0	\N	74250000.00	Cái	4500000.00	18	21	43
44	150	BATCH-2618-22	\N	\N	150	150	0	\N	29700000.00	Cái	180000.00	18	22	44
45	300	BATCH-2619-18	\N	\N	300	300	0	\N	31350000.00	Cái	95000.00	19	18	45
46	600	BATCH-2619-19	\N	\N	600	600	0	\N	29700000.00	Cái	45000.00	19	19	46
47	4	BATCH-2620-6	\N	\N	4	4	0	\N	198000000.00	Cái	45000000.00	20	6	47
48	50	BATCH-2620-7	\N	\N	50	50	0	\N	17600000.00	Cái	320000.00	20	7	48
49	10	BATCH-2621-1	\N	\N	10	10	0	\N	93500000.00	Cái	8500000.00	21	1	49
50	100	BATCH-2621-2	\N	\N	100	100	0	\N	49500000.00	Cái	450000.00	21	2	50
51	50	BATCH-2621-24	\N	\N	50	50	0	\N	20900000.00	Cái	380000.00	21	24	51
52	5	\N	\N	\N	6	5	0	\N	92500000.00	Cuộn	18500000.00	22	16	62
53	65	\N	\N	\N	70	65	0	\N	55250000.00	Cái	850000.00	22	17	63
54	5	LOT001	2026-12-31	\N	10	5	0	\N	42500000.00		8500000.00	23	1	71
55	5	L001	2026-05-31	\N	10	5	0	\N	42500000.00		8500000.00	24	1	72
56	1	L002	2026-05-02	\N	1	1	0	\N	8500000.00		8500000.00	25	1	73
57	3	L002	\N	\N	5	3	0	\N	25500000.00		8500000.00	26	1	74
58	4	L004	2026-05-02	\N	6	4	0	\N	34000000.00		8500000.00	27	1	75
59	3	001	\N	\N	3	3	0	\N	1350000.00		450000.00	28	2	77
\.


--
-- Data for Name: inventory; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.inventory (id, average_cost, last_issued_date, last_received_date, quantity_available, quantity_on_hand, quantity_reserved, reorder_level, reorder_quantity, updated_at, version, product_id, warehouse_id) FROM stdin;
26	18500000.00	\N	2026-03-31 22:31:11.977919	5	5	0	\N	\N	2026-03-31 22:31:11.980878	0	16	1
27	850000.00	\N	2026-03-31 22:31:11.994458	65	65	0	\N	\N	2026-03-31 22:31:11.9945	0	17	1
2	450000.00	\N	2026-03-15 00:00:00	150	180	30	50	100	2026-03-28 18:26:55.16767	0	2	1
4	95000.00	\N	2026-02-26 00:00:00	1900	2200	300	500	1000	2026-03-28 18:26:55.16767	0	4	1
5	180000.00	\N	2026-02-26 00:00:00	800	1000	200	200	500	2026-03-28 18:26:55.16767	0	5	1
6	45000000.00	\N	2026-03-10 00:00:00	4	6	2	2	5	2026-03-28 18:26:55.16767	0	6	2
7	320000.00	\N	2026-03-10 00:00:00	90	110	20	30	100	2026-03-28 18:26:55.16767	0	7	2
8	280000.00	\N	2026-02-25 00:00:00	330	380	50	100	200	2026-03-28 18:26:55.16767	0	8	1
9	350000.00	\N	2026-02-25 00:00:00	190	220	30	80	150	2026-03-28 18:26:55.16767	0	9	1
10	650000.00	\N	2026-02-28 00:00:00	340	420	80	100	200	2026-03-28 18:26:55.16767	0	10	2
11	1200000.00	\N	2026-02-28 00:00:00	150	190	40	50	100	2026-03-28 18:26:55.16767	0	11	2
12	280000.00	\N	2026-02-22 00:00:00	900	1100	200	300	500	2026-03-28 18:26:55.16767	0	12	1
13	1850000.00	\N	2026-02-22 00:00:00	90	110	20	30	80	2026-03-28 18:26:55.16767	0	13	1
14	980000.00	\N	2026-03-03 00:00:00	130	160	30	40	100	2026-03-28 18:26:55.16767	0	14	1
15	2200000.00	\N	2026-03-03 00:00:00	48	58	10	15	30	2026-03-28 18:26:55.16767	0	15	1
16	18500000.00	\N	2026-03-05 00:00:00	6	8	2	3	5	2026-03-28 18:26:55.16767	0	16	3
17	850000.00	\N	2026-03-05 00:00:00	70	85	15	20	50	2026-03-28 18:26:55.16767	0	17	3
18	95000.00	\N	2026-03-07 00:00:00	370	450	80	100	200	2026-03-28 18:26:55.16767	0	18	2
19	45000.00	\N	2026-03-07 00:00:00	850	1000	150	200	500	2026-03-28 18:26:55.16767	0	19	2
20	85000.00	\N	2026-03-06 00:00:00	660	760	100	200	500	2026-03-28 18:26:55.16767	0	20	1
21	4500000.00	\N	2026-03-06 00:00:00	19	24	5	5	15	2026-03-28 18:26:55.16767	0	21	1
22	180000.00	\N	2026-03-06 00:00:00	190	230	40	50	150	2026-03-28 18:26:55.16767	0	22	1
23	1200000.00	\N	2026-03-15 00:00:00	20	25	5	5	20	2026-03-28 18:26:55.16767	0	23	1
24	380000.00	\N	2026-03-15 00:00:00	60	70	10	20	50	2026-03-28 18:26:55.16767	0	24	1
25	320000.00	\N	2026-01-25 00:00:00	15	18	3	5	20	2026-03-28 18:26:55.16767	0	25	2
3	12000000.00	\N	2026-03-15 00:00:00	2	5	3	2	5	2026-05-29 06:49:46.666826	1	3	1
1	8500000.00	2026-05-01 17:36:34.719139	2026-05-01 17:22:16.227187	0	24	24	3	10	2026-05-30 14:51:40.44255	13	1	1
\.


--
-- Data for Name: inventory_lot; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.inventory_lot (id, created_at, expiry_date, lot_number, manufacture_date, quantity_received, quantity_remaining, unit_cost, updated_at, product_id, source_receipt_id, source_receipt_item_id, warehouse_id) FROM stdin;
7	2026-05-01 17:17:08.705442	\N	L002	\N	3.000	3.000	8500000.00	2026-05-01 17:17:08.70545	1	26	57	1
6	2026-05-01 17:13:48.710946	2026-05-02	L002	\N	1.000	0.000	8500000.00	2026-05-01 17:36:34.721668	1	25	56	1
8	2026-05-01 17:22:16.234832	2026-05-02	L004	\N	4.000	0.000	8500000.00	2026-05-01 17:36:34.721698	1	27	58	1
5	2026-05-01 17:13:16.724978	2026-05-31	L001	\N	5.000	3.000	8500000.00	2026-05-01 17:36:34.721714	1	24	55	1
\.


--
-- Data for Name: inventory_reservation; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.inventory_reservation (id, product_id, warehouse_id, sales_order_id, sales_order_item_id, reserved_quantity, fulfilled_quantity, status, reserved_at, expires_at, released_at, notes) FROM stdin;
\.


--
-- Data for Name: inventory_transaction; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.inventory_transaction (id, created_by, notes, quantity, quantity_after, quantity_before, reference_code, reference_id, reference_type, total_cost, transaction_date, transaction_type, unit_cost, product_id, warehouse_id) FROM stdin;
1	11	Nhập kho theo phiếu GR-2026-001	5	5	0	GR-2026-001	\N	GOODS_RECEIPT	46750000.00	2026-01-15 16:30:00	RECEIPT	8500000.00	1	1
2	11	Nhập kho theo phiếu GR-2026-001	50	50	0	GR-2026-001	\N	GOODS_RECEIPT	24750000.00	2026-01-15 16:30:00	RECEIPT	450000.00	2	1
3	11	Nhập kho theo phiếu GR-2026-001	10	10	0	GR-2026-001	\N	GOODS_RECEIPT	13200000.00	2026-01-15 16:30:00	RECEIPT	1200000.00	23	1
4	11	Nhập kho theo phiếu GR-2026-001	30	30	0	GR-2026-001	\N	GOODS_RECEIPT	12540000.00	2026-01-15 16:30:00	RECEIPT	380000.00	24	1
5	11	Nhập kho theo phiếu GR-2026-001	2	2	0	GR-2026-001	\N	GOODS_RECEIPT	26400000.00	2026-01-15 16:30:00	RECEIPT	12000000.00	3	1
6	11	Nhập kho theo phiếu GR-2026-002	1000	1000	0	GR-2026-002	\N	GOODS_RECEIPT	104500000.00	2026-01-20 14:00:00	RECEIPT	95000.00	4	1
7	11	Nhập kho theo phiếu GR-2026-002	500	500	0	GR-2026-002	\N	GOODS_RECEIPT	99000000.00	2026-01-20 14:00:00	RECEIPT	180000.00	5	1
8	11	Nhập kho theo phiếu GR-2026-003	3	3	0	GR-2026-003	\N	GOODS_RECEIPT	148500000.00	2026-01-25 11:00:00	RECEIPT	45000000.00	6	2
9	11	Nhập kho theo phiếu GR-2026-003	80	80	0	GR-2026-003	\N	GOODS_RECEIPT	28160000.00	2026-01-25 11:00:00	RECEIPT	320000.00	7	2
10	11	Nhập kho theo phiếu GR-2026-003	20	20	0	GR-2026-003	\N	GOODS_RECEIPT	7040000.00	2026-01-25 11:00:00	RECEIPT	320000.00	25	2
11	11	Nhập kho theo phiếu GR-2026-004	150	150	0	GR-2026-004	\N	GOODS_RECEIPT	46200000.00	2026-01-28 15:00:00	RECEIPT	280000.00	8	1
12	11	Nhập kho theo phiếu GR-2026-004	100	100	0	GR-2026-004	\N	GOODS_RECEIPT	38500000.00	2026-01-28 15:00:00	RECEIPT	350000.00	9	1
13	11	Nhập kho theo phiếu GR-2026-005	200	200	0	GR-2026-005	\N	GOODS_RECEIPT	143000000.00	2026-01-30 16:00:00	RECEIPT	650000.00	10	2
14	11	Nhập kho theo phiếu GR-2026-005	100	100	0	GR-2026-005	\N	GOODS_RECEIPT	132000000.00	2026-01-30 16:00:00	RECEIPT	1200000.00	11	2
15	11	Nhập kho theo phiếu GR-2026-006	500	500	0	GR-2026-006	\N	GOODS_RECEIPT	154000000.00	2026-01-31 17:00:00	RECEIPT	280000.00	12	1
16	11	Nhập kho theo phiếu GR-2026-006	60	60	0	GR-2026-006	\N	GOODS_RECEIPT	122100000.00	2026-01-31 17:00:00	RECEIPT	1850000.00	13	1
17	11	Nhập kho theo phiếu GR-2026-007	80	80	0	GR-2026-007	\N	GOODS_RECEIPT	86240000.00	2026-02-05 14:30:00	RECEIPT	980000.00	14	1
18	11	Nhập kho theo phiếu GR-2026-007	30	30	0	GR-2026-007	\N	GOODS_RECEIPT	72600000.00	2026-02-05 14:30:00	RECEIPT	2200000.00	15	1
19	11	Nhập kho theo phiếu GR-2026-008	4	4	0	GR-2026-008	\N	GOODS_RECEIPT	81400000.00	2026-02-08 16:00:00	RECEIPT	18500000.00	16	3
20	11	Nhập kho theo phiếu GR-2026-008	40	40	0	GR-2026-008	\N	GOODS_RECEIPT	37400000.00	2026-02-08 16:00:00	RECEIPT	850000.00	17	3
21	11	Nhập kho theo phiếu GR-2026-009	200	200	0	GR-2026-009	\N	GOODS_RECEIPT	20900000.00	2026-02-10 11:00:00	RECEIPT	95000.00	18	2
22	11	Nhập kho theo phiếu GR-2026-009	500	500	0	GR-2026-009	\N	GOODS_RECEIPT	24750000.00	2026-02-10 11:00:00	RECEIPT	45000.00	19	2
23	11	Nhập kho theo phiếu GR-2026-010	300	300	0	GR-2026-010	\N	GOODS_RECEIPT	28050000.00	2026-02-12 15:00:00	RECEIPT	85000.00	20	1
24	11	Nhập kho theo phiếu GR-2026-010	10	10	0	GR-2026-010	\N	GOODS_RECEIPT	49500000.00	2026-02-12 15:00:00	RECEIPT	4500000.00	21	1
25	11	Nhập kho theo phiếu GR-2026-010	100	100	0	GR-2026-010	\N	GOODS_RECEIPT	19800000.00	2026-02-12 15:00:00	RECEIPT	180000.00	22	1
26	11	Nhập kho theo phiếu GR-2026-011	8	8	0	GR-2026-011	\N	GOODS_RECEIPT	74800000.00	2026-02-18 16:00:00	RECEIPT	8500000.00	1	1
27	11	Nhập kho theo phiếu GR-2026-011	80	80	0	GR-2026-011	\N	GOODS_RECEIPT	39600000.00	2026-02-18 16:00:00	RECEIPT	450000.00	2	1
28	11	Nhập kho theo phiếu GR-2026-011	3	3	0	GR-2026-011	\N	GOODS_RECEIPT	39600000.00	2026-02-18 16:00:00	RECEIPT	12000000.00	3	1
29	11	Nhập kho theo phiếu GR-2026-011	15	15	0	GR-2026-011	\N	GOODS_RECEIPT	19800000.00	2026-02-18 16:00:00	RECEIPT	1200000.00	23	1
30	11	Nhập kho theo phiếu GR-2026-012	700	700	0	GR-2026-012	\N	GOODS_RECEIPT	215600000.00	2026-02-22 15:30:00	RECEIPT	280000.00	12	1
31	11	Nhập kho theo phiếu GR-2026-012	80	80	0	GR-2026-012	\N	GOODS_RECEIPT	162800000.00	2026-02-22 15:30:00	RECEIPT	1850000.00	13	1
32	11	Nhập kho theo phiếu GR-2026-013	200	200	0	GR-2026-013	\N	GOODS_RECEIPT	61600000.00	2026-02-25 14:00:00	RECEIPT	280000.00	8	1
33	11	Nhập kho theo phiếu GR-2026-013	150	150	0	GR-2026-013	\N	GOODS_RECEIPT	57750000.00	2026-02-25 14:00:00	RECEIPT	350000.00	9	1
34	11	Nhập kho theo phiếu GR-2026-014	1200	1200	0	GR-2026-014	\N	GOODS_RECEIPT	125400000.00	2026-02-26 16:30:00	RECEIPT	95000.00	4	2
35	11	Nhập kho theo phiếu GR-2026-014	600	600	0	GR-2026-014	\N	GOODS_RECEIPT	118800000.00	2026-02-26 16:30:00	RECEIPT	180000.00	5	2
36	11	Nhập kho theo phiếu GR-2026-015	250	250	0	GR-2026-015	\N	GOODS_RECEIPT	178750000.00	2026-02-28 15:00:00	RECEIPT	650000.00	10	2
37	11	Nhập kho theo phiếu GR-2026-015	120	120	0	GR-2026-015	\N	GOODS_RECEIPT	144000000.00	2026-02-28 15:00:00	RECEIPT	1200000.00	11	2
38	11	Nhập kho theo phiếu GR-2026-016	100	100	0	GR-2026-016	\N	GOODS_RECEIPT	107800000.00	2026-03-03 14:00:00	RECEIPT	980000.00	14	1
39	11	Nhập kho theo phiếu GR-2026-016	35	35	0	GR-2026-016	\N	GOODS_RECEIPT	84700000.00	2026-03-03 14:00:00	RECEIPT	2200000.00	15	1
40	11	Nhập kho theo phiếu GR-2026-017	5	5	0	GR-2026-017	\N	GOODS_RECEIPT	101750000.00	2026-03-05 16:00:00	RECEIPT	18500000.00	16	3
41	11	Nhập kho theo phiếu GR-2026-017	60	60	0	GR-2026-017	\N	GOODS_RECEIPT	56100000.00	2026-03-05 16:00:00	RECEIPT	850000.00	17	3
42	11	Nhập kho theo phiếu GR-2026-018	500	500	0	GR-2026-018	\N	GOODS_RECEIPT	46750000.00	2026-03-06 15:00:00	RECEIPT	85000.00	20	1
43	11	Nhập kho theo phiếu GR-2026-018	15	15	0	GR-2026-018	\N	GOODS_RECEIPT	74250000.00	2026-03-06 15:00:00	RECEIPT	4500000.00	21	1
44	11	Nhập kho theo phiếu GR-2026-018	150	150	0	GR-2026-018	\N	GOODS_RECEIPT	29700000.00	2026-03-06 15:00:00	RECEIPT	180000.00	22	1
45	11	Nhập kho theo phiếu GR-2026-019	300	300	0	GR-2026-019	\N	GOODS_RECEIPT	31350000.00	2026-03-07 11:00:00	RECEIPT	95000.00	18	2
46	11	Nhập kho theo phiếu GR-2026-019	600	600	0	GR-2026-019	\N	GOODS_RECEIPT	29700000.00	2026-03-07 11:00:00	RECEIPT	45000.00	19	2
47	11	Nhập kho theo phiếu GR-2026-020	4	4	0	GR-2026-020	\N	GOODS_RECEIPT	198000000.00	2026-03-10 16:00:00	RECEIPT	45000000.00	6	2
48	11	Nhập kho theo phiếu GR-2026-020	50	50	0	GR-2026-020	\N	GOODS_RECEIPT	17600000.00	2026-03-10 16:00:00	RECEIPT	320000.00	7	2
49	11	Nhập kho theo phiếu GR-2026-021	10	10	0	GR-2026-021	\N	GOODS_RECEIPT	93500000.00	2026-03-15 16:00:00	RECEIPT	8500000.00	1	1
50	11	Nhập kho theo phiếu GR-2026-021	100	100	0	GR-2026-021	\N	GOODS_RECEIPT	49500000.00	2026-03-15 16:00:00	RECEIPT	450000.00	2	1
51	11	Nhập kho theo phiếu GR-2026-021	50	50	0	GR-2026-021	\N	GOODS_RECEIPT	20900000.00	2026-03-15 16:00:00	RECEIPT	380000.00	24	1
52	11	Xuất kho theo phiếu GI-2026-001	3	0	3	GI-2026-001	\N	GOODS_ISSUE	31350000.00	2026-01-22 15:00:00	ISSUE	9500000.00	1	1
53	11	Xuất kho theo phiếu GI-2026-001	30	0	30	GI-2026-001	\N	GOODS_ISSUE	16500000.00	2026-01-22 15:00:00	ISSUE	500000.00	2	1
54	11	Xuất kho theo phiếu GI-2026-001	8	0	8	GI-2026-001	\N	GOODS_ISSUE	11880000.00	2026-01-22 15:00:00	ISSUE	1350000.00	23	1
55	11	Xuất kho theo phiếu GI-2026-001	20	0	20	GI-2026-001	\N	GOODS_ISSUE	9240000.00	2026-01-22 15:00:00	ISSUE	420000.00	24	1
56	11	Xuất kho theo phiếu GI-2026-002	5	0	5	GI-2026-002	\N	GOODS_ISSUE	52250000.00	2026-01-23 16:00:00	ISSUE	9500000.00	1	1
57	11	Xuất kho theo phiếu GI-2026-002	3	0	3	GI-2026-002	\N	GOODS_ISSUE	44550000.00	2026-01-23 16:00:00	ISSUE	13500000.00	3	1
58	11	Xuất kho theo phiếu GI-2026-002	50	0	50	GI-2026-002	\N	GOODS_ISSUE	27500000.00	2026-01-23 16:00:00	ISSUE	500000.00	2	1
59	11	Xuất kho theo phiếu GI-2026-003	200	0	200	GI-2026-003	\N	GOODS_ISSUE	70400000.00	2026-01-26 14:00:00	ISSUE	320000.00	12	1
60	11	Xuất kho theo phiếu GI-2026-003	25	0	25	GI-2026-003	\N	GOODS_ISSUE	57750000.00	2026-01-26 14:00:00	ISSUE	2100000.00	13	1
61	11	Xuất kho theo phiếu GI-2026-003	20	0	20	GI-2026-003	\N	GOODS_ISSUE	24200000.00	2026-01-26 14:00:00	ISSUE	1100000.00	14	1
62	11	Xuất kho theo phiếu GI-2026-004	100	0	100	GI-2026-004	\N	GOODS_ISSUE	34100000.00	2026-01-27 16:00:00	ISSUE	310000.00	8	1
63	11	Xuất kho theo phiếu GI-2026-004	50	0	50	GI-2026-004	\N	GOODS_ISSUE	39600000.00	2026-01-27 16:00:00	ISSUE	720000.00	10	1
64	11	Xuất kho theo phiếu GI-2026-004	50	0	50	GI-2026-004	\N	GOODS_ISSUE	21450000.00	2026-01-27 16:00:00	ISSUE	390000.00	9	1
65	11	Xuất kho theo phiếu GI-2026-005	2	0	2	GI-2026-005	\N	GOODS_ISSUE	110000000.00	2026-01-28 17:00:00	ISSUE	50000000.00	6	2
66	11	Xuất kho theo phiếu GI-2026-005	60	0	60	GI-2026-005	\N	GOODS_ISSUE	23760000.00	2026-01-28 17:00:00	ISSUE	360000.00	7	2
67	11	Xuất kho theo phiếu GI-2026-005	15	0	15	GI-2026-005	\N	GOODS_ISSUE	5940000.00	2026-01-28 17:00:00	ISSUE	360000.00	25	2
68	11	Xuất kho theo phiếu GI-2026-006	800	0	800	GI-2026-006	\N	GOODS_ISSUE	92400000.00	2026-01-29 16:00:00	ISSUE	105000.00	4	1
69	11	Xuất kho theo phiếu GI-2026-006	400	0	400	GI-2026-006	\N	GOODS_ISSUE	88000000.00	2026-01-29 16:00:00	ISSUE	200000.00	5	1
70	11	Xuất kho theo phiếu GI-2026-007	200	0	200	GI-2026-007	\N	GOODS_ISSUE	20900000.00	2026-01-30 15:00:00	ISSUE	95000.00	20	1
71	11	Xuất kho theo phiếu GI-2026-007	150	0	150	GI-2026-007	\N	GOODS_ISSUE	17325000.00	2026-01-30 15:00:00	ISSUE	105000.00	18	1
72	11	Xuất kho theo phiếu GI-2026-007	120	0	120	GI-2026-007	\N	GOODS_ISSUE	42240000.00	2026-01-30 15:00:00	ISSUE	320000.00	12	1
73	11	Xuất kho theo phiếu GI-2026-007	10	0	10	GI-2026-007	\N	GOODS_ISSUE	23100000.00	2026-01-30 15:00:00	ISSUE	2100000.00	13	1
74	11	Xuất kho theo phiếu GI-2026-008	400	0	400	GI-2026-008	\N	GOODS_ISSUE	140800000.00	2026-01-31 17:00:00	ISSUE	320000.00	12	1
75	11	Xuất kho theo phiếu GI-2026-008	40	0	40	GI-2026-008	\N	GOODS_ISSUE	92400000.00	2026-01-31 17:00:00	ISSUE	2100000.00	13	1
76	11	Xuất kho theo phiếu GI-2026-009	150	0	150	GI-2026-009	\N	GOODS_ISSUE	17325000.00	2026-02-02 15:00:00	ISSUE	105000.00	18	2
77	11	Xuất kho theo phiếu GI-2026-009	100	0	100	GI-2026-009	\N	GOODS_ISSUE	10450000.00	2026-02-02 15:00:00	ISSUE	95000.00	20	2
78	11	Xuất kho theo phiếu GI-2026-009	30	0	30	GI-2026-009	\N	GOODS_ISSUE	13860000.00	2026-02-02 15:00:00	ISSUE	420000.00	24	2
79	11	Xuất kho theo phiếu GI-2026-009	10	0	10	GI-2026-009	\N	GOODS_ISSUE	3960000.00	2026-02-02 15:00:00	ISSUE	360000.00	25	2
80	11	Xuất kho theo phiếu GI-2026-010	2	0	2	GI-2026-010	\N	GOODS_ISSUE	45100000.00	2026-02-04 16:00:00	ISSUE	20500000.00	16	1
81	11	Xuất kho theo phiếu GI-2026-010	25	0	25	GI-2026-010	\N	GOODS_ISSUE	26125000.00	2026-02-04 16:00:00	ISSUE	950000.00	17	1
82	11	Xuất kho theo phiếu GI-2026-010	8	0	8	GI-2026-010	\N	GOODS_ISSUE	3168000.00	2026-02-04 16:00:00	ISSUE	360000.00	25	1
83	11	Xuất kho theo phiếu GI-2026-011	4	0	4	GI-2026-011	\N	GOODS_ISSUE	41800000.00	2026-02-21 16:00:00	ISSUE	9500000.00	1	1
84	11	Xuất kho theo phiếu GI-2026-011	2	0	2	GI-2026-011	\N	GOODS_ISSUE	29700000.00	2026-02-21 16:00:00	ISSUE	13500000.00	3	1
85	11	Xuất kho theo phiếu GI-2026-011	10	0	10	GI-2026-011	\N	GOODS_ISSUE	14850000.00	2026-02-21 16:00:00	ISSUE	1350000.00	23	1
86	11	Xuất kho theo phiếu GI-2026-012	1000	0	1000	GI-2026-012	\N	GOODS_ISSUE	115500000.00	2026-02-24 14:00:00	ISSUE	105000.00	4	1
87	11	Xuất kho theo phiếu GI-2026-012	500	0	500	GI-2026-012	\N	GOODS_ISSUE	110000000.00	2026-02-24 14:00:00	ISSUE	200000.00	5	1
88	11	Xuất kho theo phiếu GI-2026-013	450	0	450	GI-2026-013	\N	GOODS_ISSUE	158400000.00	2026-02-26 15:00:00	ISSUE	320000.00	12	1
89	11	Xuất kho theo phiếu GI-2026-013	50	0	50	GI-2026-013	\N	GOODS_ISSUE	115500000.00	2026-02-26 15:00:00	ISSUE	2100000.00	13	1
90	11	Xuất kho theo phiếu GI-2026-014	3	0	3	GI-2026-014	\N	GOODS_ISSUE	67650000.00	2026-02-27 16:00:00	ISSUE	20500000.00	16	1
91	11	Xuất kho theo phiếu GI-2026-014	30	0	30	GI-2026-014	\N	GOODS_ISSUE	31350000.00	2026-02-27 16:00:00	ISSUE	950000.00	17	1
92	11	Xuất kho theo phiếu GI-2026-015	120	0	120	GI-2026-015	\N	GOODS_ISSUE	40920000.00	2026-03-01 15:00:00	ISSUE	310000.00	8	1
93	11	Xuất kho theo phiếu GI-2026-015	80	0	80	GI-2026-015	\N	GOODS_ISSUE	34320000.00	2026-03-01 15:00:00	ISSUE	390000.00	9	1
94	11	Xuất kho theo phiếu GI-2026-016	60	0	60	GI-2026-016	\N	GOODS_ISSUE	72600000.00	2026-03-02 14:00:00	ISSUE	1100000.00	14	1
95	11	Xuất kho theo phiếu GI-2026-016	25	0	25	GI-2026-016	\N	GOODS_ISSUE	68750000.00	2026-03-02 14:00:00	ISSUE	2500000.00	2	1
96	11	Xuất kho theo phiếu GI-2026-017	80	0	80	GI-2026-017	\N	GOODS_ISSUE	63360000.00	2026-03-03 16:00:00	ISSUE	720000.00	10	2
97	11	Xuất kho theo phiếu GI-2026-017	60	0	60	GI-2026-017	\N	GOODS_ISSUE	23760000.00	2026-03-03 16:00:00	ISSUE	360000.00	7	2
98	11	Xuất kho theo phiếu GI-2026-018	300	0	300	GI-2026-018	\N	GOODS_ISSUE	31350000.00	2026-03-05 15:00:00	ISSUE	95000.00	20	1
99	11	Xuất kho theo phiếu GI-2026-018	200	0	200	GI-2026-018	\N	GOODS_ISSUE	23100000.00	2026-03-05 15:00:00	ISSUE	105000.00	18	1
100	11	Xuất kho theo phiếu GI-2026-018	30	0	30	GI-2026-018	\N	GOODS_ISSUE	13860000.00	2026-03-05 15:00:00	ISSUE	420000.00	24	1
101	11	Xuất kho theo phiếu GI-2026-018	10	0	10	GI-2026-018	\N	GOODS_ISSUE	3960000.00	2026-03-05 15:00:00	ISSUE	360000.00	25	1
102	11	Xuất kho theo phiếu GI-2026-019	3	0	3	GI-2026-019	\N	GOODS_ISSUE	165000000.00	2026-03-07 16:00:00	ISSUE	50000000.00	6	2
103	11	Xuất kho theo phiếu GI-2026-019	50	0	50	GI-2026-019	\N	GOODS_ISSUE	19800000.00	2026-03-07 16:00:00	ISSUE	360000.00	7	2
104	11	Xuất kho theo phiếu GI-2026-019	10	0	10	GI-2026-019	\N	GOODS_ISSUE	3960000.00	2026-03-07 16:00:00	ISSUE	360000.00	25	2
105	11	Xuất kho theo phiếu GI-2026-020	50	0	50	GI-2026-020	\N	GOODS_ISSUE	39600000.00	2026-03-09 14:00:00	ISSUE	720000.00	10	2
106	11	Xuất kho theo phiếu GI-2026-020	60	0	60	GI-2026-020	\N	GOODS_ISSUE	20460000.00	2026-03-09 14:00:00	ISSUE	310000.00	8	2
107	11	Xuất kho theo phiếu GI-2026-021	6	0	6	GI-2026-021	\N	GOODS_ISSUE	62700000.00	2026-03-21 16:00:00	ISSUE	9500000.00	1	1
108	11	Xuất kho theo phiếu GI-2026-021	4	0	4	GI-2026-021	\N	GOODS_ISSUE	59400000.00	2026-03-21 16:00:00	ISSUE	13500000.00	3	1
109	11	Xuất kho theo phiếu GI-2026-021	60	0	60	GI-2026-021	\N	GOODS_ISSUE	33000000.00	2026-03-21 16:00:00	ISSUE	500000.00	2	1
110	\N	Stock received from GOODS_RECEIPT	5	5	0	GR-1774971048925	22	GOODS_RECEIPT	92500000.00	2026-03-31 22:31:11.986698	RECEIPT	18500000.00	16	1
111	\N	Stock received from GOODS_RECEIPT	65	65	0	GR-1774971048925	22	GOODS_RECEIPT	55250000.00	2026-03-31 22:31:11.995168	RECEIPT	850000.00	17	1
112	\N	Stock received from GOODS_RECEIPT	5	23	18	GR-1777630385574	24	GOODS_RECEIPT	42500000.00	2026-05-01 17:13:16.714729	RECEIPT	8500000.00	1	1
113	\N	Stock received from GOODS_RECEIPT	1	24	23	GR-1777630426491	25	GOODS_RECEIPT	8500000.00	2026-05-01 17:13:48.704111	RECEIPT	8500000.00	1	1
114	\N	Stock received from GOODS_RECEIPT	3	27	24	GR-1777630624189	26	GOODS_RECEIPT	25500000.00	2026-05-01 17:17:08.701116	RECEIPT	8500000.00	1	1
115	\N	Stock received from GOODS_RECEIPT	4	31	27	GR-1777630932731	27	GOODS_RECEIPT	34000000.00	2026-05-01 17:22:16.230239	RECEIPT	8500000.00	1	1
116	\N	Stock issued for GOODS_ISSUE	7	24	31	GI-1777631788989	22	GOODS_ISSUE	59500000.00	2026-05-01 17:36:34.719851	ISSUE	8500000.00	1	1
\.


--
-- Data for Name: invoice; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.invoice (id, code, invoice_date, status, total_amount, purchase_order_id, supplier_id) FROM stdin;
\.


--
-- Data for Name: product; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.product (id, code, description, name, price, quantity, supplier_id) FROM stdin;
1	PRD001	PLC Siemens S7-300, CPU 314C-2 DP	Board mạch điều khiển PLC S7-300	8500000	0	1
2	PRD002	Cảm biến PT100, dải đo -50 đến 200°C, IP67	Cảm biến nhiệt độ PT100	450000	0	1
3	PRD003	Biến tần 3 pha 380V, 5.5kW, IP21	Biến tần ABB ACS550 5.5kW	12000000	0	1
4	PRD004	Xi măng Portland PC40, bao 50kg	Xi măng Hà Tiên 50kg	95000	0	2
5	PRD005	Gạch ceramic men bóng, kích thước 60x60cm	Gạch ceramic 60x60 cao cấp	180000	0	2
6	PRD006	Máy bơm ly tâm, lưu lượng 120 m3/h, cột áp 30m	Máy bơm công nghiệp GRUNDFOS 15kW	45000000	0	3
7	PRD007	Van bi inox 304, DN50, PN16, tay vặn	Van bi inox 304 DN50	320000	0	3
8	PRD008	Natri Hydroxide công nghiệp, bao 25kg	Hóa chất xử lý nước NaOH 99%	280000	0	4
9	PRD009	H2SO4 98%, can 35kg	Acid Sulfuric 98% công nghiệp	350000	0	4
10	PRD010	Thùng nhựa HDPE trắng, dung tích 200L, có nắp	Thùng nhựa HDPE 200L	650000	0	5
11	PRD011	Pallet nhựa HDPE, tải trọng 1500kg, 4 chiều vào	Pallet nhựa 1200x1000	1200000	0	5
12	PRD012	Thép hộp mạ kẽm, 50x50x2mm, cây 6m	Thép hộp 50x50x2mm	280000	0	6
13	PRD013	Thép tấm cán nóng SS400, dày 3mm, 1220x2440mm	Thép tấm SS400 3mm	1850000	0	6
14	PRD014	Ống inox 304, phi 76, dày 2mm, cây 6m	Ống inox 304 phi 76 x 2mm	980000	0	7
15	PRD015	Tấm inox 304 2B, dày 1.5mm, 1000x2000mm	Tấm inox 304 1.5mm	2200000	0	7
16	PRD016	Cáp Cu/PVC/PVC hạ thế, 3x50mm2, cuộn 100m	Cáp điện CADIVI 3x50mm2	18500000	0	8
17	PRD017	MCB 3 pha, 100A, 6kA, DIN rail	Aptomat MCB CHINT 3P 100A	850000	0	8
18	PRD018	Dây curoa hình thang, tiết diện B, dài 70 inch	Dây curoa cao su Type B70	95000	0	9
19	PRD019	Gioăng EPDM, DN100, chịu nhiệt 150°C	Gioăng cao su chịu nhiệt EPDM DN100	45000	0	9
20	PRD020	Vòng bi cầu 1 dãy, che kín 2 mặt, 25x52x15mm	Vòng bi SKF 6205-2RS	85000	0	10
21	PRD021	Vít me bi THK BNF1520-8, bước 20mm, dài 1500mm	Vít me bi THK BNF1520	4500000	0	10
22	PRD022	Khớp nối đàn hồi GE28, lỗ 14-19mm	Khớp nối đàn hồi GE28	180000	0	10
23	PRD023	Encoder tăng lượng 600P/R, trục 6mm, 5-24VDC	Encoder Autonics E40S6	1200000	0	1
24	PRD024	Relay nhiệt, dải 28-40A, reset tay/tự động	Relay nhiệt LS MT-32 28-40A	380000	0	8
25	PRD025	Đồng hồ áp suất mặt 100mm, 0-10 bar, kết nối 1/2"	Đồng hồ áp suất Wise P110 0-10 bar	320000	0	3
26	PROD001	Standard widget	Widget A	25	100	1
27	PROD002	Premium widget	Widget B	45	50	1
28	PROD003	Essential component	Component X	12.5	200	2
29	PROD004	Advanced component	Component Y	35	75	2
30	PROD005	Replacement part	Part Z	8	150	3
\.


--
-- Data for Name: purchase_order; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.purchase_order (id, approved_by, approved_date, code, completed_date, created_by, created_date, delivery_date, invoice_number, notes, order_name, rejection_reason, shipping_cost, status, tax_type, total_amount, updated_at, supplier_id, warehouse_id) FROM stdin;
1	8	2026-01-04 09:00:00	PO-2026-001	2026-01-15 16:30:00	7	2026-01-03	2026-01-15 00:00:00	\N	Ưu tiên giao hàng đúng hạn	Mua linh kiện Q1/2026 - Lô 1	\N	2500000.00	ORDER_COMPLETED	\N	158250000.00	\N	1	1
2	8	2026-01-06 10:30:00	PO-2026-002	2026-01-20 14:00:00	7	2026-01-05	2026-01-20 00:00:00	\N	Giao theo 2 chuyến	Mua VLXD tháng 1/2026	\N	5000000.00	ORDER_COMPLETED	\N	215000000.00	\N	2	1
3	8	2026-01-08 08:00:00	PO-2026-003	2026-01-25 11:00:00	7	2026-01-07	2026-01-25 00:00:00	\N	Hàng nhập khẩu, kiểm tra kỹ trước nhận	Mua thiết bị bơm & van T1/2026	\N	3000000.00	ORDER_COMPLETED	\N	180500000.00	\N	3	2
4	8	2026-01-09 09:00:00	PO-2026-004	2026-01-28 15:00:00	7	2026-01-08	2026-01-28 00:00:00	\N	Lưu kho hóa chất chuyên dụng	Mua hóa chất công nghiệp T1/2026	\N	1500000.00	ORDER_COMPLETED	\N	95700000.00	\N	4	1
5	8	2026-01-11 10:00:00	PO-2026-005	2026-01-30 16:00:00	7	2026-01-10	2026-01-30 00:00:00	\N	\N	Mua bao bì nhựa tháng 1/2026	\N	2000000.00	ORDER_COMPLETED	\N	182050000.00	\N	5	2
6	8	2026-01-13 08:30:00	PO-2026-006	2026-01-31 17:00:00	7	2026-01-12	2026-01-31 00:00:00	\N	Thép nhập từ Formosa	Mua thép kết cấu T1/2026	\N	4000000.00	ORDER_COMPLETED	\N	297000000.00	\N	6	1
7	8	2026-01-16 09:00:00	PO-2026-007	2026-02-05 14:30:00	7	2026-01-15	2026-02-05 00:00:00	\N	\N	Mua inox 304 tháng 1/2026	\N	2500000.00	ORDER_COMPLETED	\N	158950000.00	\N	7	1
8	8	2026-01-19 10:00:00	PO-2026-008	2026-02-08 16:00:00	7	2026-01-18	2026-02-08 00:00:00	\N	Giao kho Hà Nội	Mua thiết bị điện T1/2026	\N	1800000.00	ORDER_COMPLETED	\N	125100000.00	\N	8	3
9	8	2026-01-21 08:00:00	PO-2026-009	2026-02-10 11:00:00	7	2026-01-20	2026-02-10 00:00:00	\N	\N	Mua sản phẩm cao su T1/2026	\N	800000.00	ORDER_COMPLETED	\N	45650000.00	\N	9	2
10	8	2026-01-23 09:30:00	PO-2026-010	2026-02-12 15:00:00	7	2026-01-22	2026-02-12 00:00:00	\N	\N	Mua linh phụ kiện cơ khí T1/2026	\N	1200000.00	ORDER_COMPLETED	\N	121200000.00	\N	10	1
11	8	2026-02-04 09:00:00	PO-2026-011	2026-02-18 16:00:00	7	2026-02-03	2026-02-18 00:00:00	\N	\N	Mua linh kiện điện tử T2/2026 - Lô 1	\N	3000000.00	ORDER_COMPLETED	\N	210650000.00	\N	1	1
12	8	2026-02-06 10:00:00	PO-2026-012	2026-02-22 15:30:00	7	2026-02-05	2026-02-22 00:00:00	\N	Lô thép lớn cho dự án mở rộng	Mua thép tháng 2/2026	\N	5000000.00	ORDER_COMPLETED	\N	396000000.00	\N	6	1
13	8	2026-02-08 08:30:00	PO-2026-013	2026-02-25 14:00:00	7	2026-02-07	2026-02-25 00:00:00	\N	\N	Mua hóa chất T2/2026	\N	2000000.00	ORDER_COMPLETED	\N	143000000.00	\N	4	1
14	8	2026-02-11 09:00:00	PO-2026-014	2026-02-26 16:30:00	7	2026-02-10	2026-02-26 00:00:00	\N	\N	Mua VLXD T2/2026	\N	4500000.00	ORDER_COMPLETED	\N	279400000.00	\N	2	2
15	8	2026-02-13 10:30:00	PO-2026-015	2026-02-28 15:00:00	7	2026-02-12	2026-02-28 00:00:00	\N	\N	Mua bao bì T2/2026	\N	2200000.00	ORDER_COMPLETED	\N	233200000.00	\N	5	2
16	8	2026-02-15 09:00:00	PO-2026-016	2026-03-03 14:00:00	7	2026-02-14	2026-03-03 00:00:00	\N	\N	Mua inox T2/2026	\N	3000000.00	ORDER_COMPLETED	\N	187000000.00	\N	7	1
17	8	2026-02-17 08:00:00	PO-2026-017	2026-03-05 16:00:00	7	2026-02-16	2026-03-05 00:00:00	\N	Bổ sung cho kho Hà Nội	Mua thiết bị điện T2/2026	\N	2000000.00	ORDER_COMPLETED	\N	163800000.00	\N	8	3
18	8	2026-02-19 09:30:00	PO-2026-018	2026-03-06 15:00:00	7	2026-02-18	2026-03-06 00:00:00	\N	\N	Mua linh phụ kiện cơ khí T2/2026	\N	1500000.00	ORDER_COMPLETED	\N	165900000.00	\N	10	1
19	8	2026-02-21 08:30:00	PO-2026-019	2026-03-07 11:00:00	7	2026-02-20	2026-03-07 00:00:00	\N	\N	Mua cao su T2/2026	\N	1000000.00	ORDER_COMPLETED	\N	60500000.00	\N	9	2
20	8	2026-02-23 10:00:00	PO-2026-020	2026-03-10 16:00:00	7	2026-02-22	2026-03-10 00:00:00	\N	Dự án mở rộng nhà máy Bình Dương	Mua thiết bị bơm T2/2026	\N	3500000.00	ORDER_COMPLETED	\N	220000000.00	\N	3	2
21	8	2026-03-04 09:00:00	PO-2026-021	2026-03-15 16:00:00	7	2026-03-03	2026-03-15 00:00:00	\N	\N	Mua linh kiện điện tử T3/2026	\N	2800000.00	ORDER_COMPLETED	\N	196350000.00	\N	1	1
22	8	2026-03-06 10:00:00	PO-2026-022	\N	7	2026-03-05	2026-03-28 00:00:00	\N	Cần hàng gấp cho dự án	Mua thép T3/2026 - Lô lớn	\N	6000000.00	ORDER_APPROVED	\N	506000000.00	\N	6	1
23	8	2026-03-08 09:00:00	PO-2026-023	\N	7	2026-03-07	2026-03-30 00:00:00	\N	\N	Mua hóa chất T3/2026	\N	2500000.00	ORDER_APPROVED	\N	165000000.00	\N	4	1
24	8	2026-03-09 10:00:00	PO-2026-024	\N	7	2026-03-08	2026-03-31 00:00:00	\N	\N	Mua inox T3/2026	\N	3200000.00	ORDER_APPROVED	\N	220000000.00	\N	7	1
25	8	2026-03-11 09:00:00	PO-2026-025	\N	7	2026-03-10	2026-04-02 00:00:00	\N	Cung cấp cho dự án nhà ở xã hội	Mua VLXD T3/2026	\N	5000000.00	ORDER_APPROVED	\N	330000000.00	\N	2	2
26	8	2026-03-12 10:00:00	PO-2026-026	\N	7	2026-03-11	2026-04-03 00:00:00	\N	\N	Mua bao bì T3/2026	\N	2500000.00	ORDER_APPROVED	\N	286000000.00	\N	5	2
28	\N	\N	PO-2026-028	\N	7	2026-03-14	2026-04-06 00:00:00	\N	\N	Mua cao su T3/2026	\N	1200000.00	ORDER_OPEN	\N	79200000.00	\N	9	2
29	\N	\N	PO-2026-029	\N	7	2026-03-15	2026-04-08 00:00:00	\N	\N	Mua linh phụ kiện T3/2026	\N	1800000.00	ORDER_OPEN	\N	195000000.00	\N	10	1
30	\N	\N	PO-2026-030	\N	7	2026-03-16	2026-04-10 00:00:00	\N	Yêu cầu báo giá lại nếu cần	Mua thiết bị bơm T3/2026	\N	4000000.00	ORDER_OPEN	\N	270000000.00	\N	3	1
27	\N	2026-03-31 22:27:44.679459	PO-2026-027	\N	7	2026-03-13	2026-04-05 00:00:00	\N	Đang chờ duyệt	Mua thiết bị điện T3/2026	\N	2000000.00	ORDER_PARTIALLY_RECEIVED	\N	190000000.00	2026-03-31 22:31:12.001252	8	1
32	\N	2026-04-30 19:10:56.463536	PO-1777550981462	\N	\N	2026-04-30	2026-05-01 00:00:00	\N	\N	test-01	\N	0.00	ORDER_APPROVED	8%	85000000.00	2026-04-30 19:10:56.472048	1	1
33	\N	2026-05-01 17:12:19.248213	PO-1777630334817	\N	\N	2026-05-01	2026-05-02 00:00:00	\N	\N	\N	\N	0.00	ORDER_PARTIALLY_RECEIVED	8%	85000000.00	2026-05-01 17:13:16.730651	1	1
34	\N	2026-05-01 17:13:36.590691	PO-1777630413292	2026-05-01 17:13:48.717486	\N	2026-05-01	2026-05-02 00:00:00	\N	\N	\N	\N	0.00	ORDER_COMPLETED	8%	8500000.00	2026-05-01 17:13:48.718239	1	1
35	\N	2026-05-01 17:16:52.455975	PO-1777630609271	\N	\N	2026-05-01	2026-05-02 00:00:00	\N	\N	\N	\N	0.00	ORDER_PARTIALLY_RECEIVED	8%	42500000.00	2026-05-01 17:17:08.711723	1	1
36	\N	2026-05-01 17:21:59.535935	PO-1777630916855	\N	\N	2026-05-01	\N	\N	\N	\N	\N	0.00	ORDER_PARTIALLY_RECEIVED	8%	51000000.00	2026-05-01 17:22:16.242362	1	1
37	\N	\N	PO-1779935087909	\N	\N	2026-05-28	\N	\N	\N		\N	0.00	ORDER_OPEN	8%	850000000.00	2026-05-28 09:24:47.912341	2	1
38	\N	2026-05-30 14:38:34.888699	PO-1780126653173	\N	\N	2026-05-30	2026-05-31 00:00:00	\N	\N	\N	\N	0.00	ORDER_APPROVED	8%	1350000.00	2026-05-30 14:38:34.890533	1	1
\.


--
-- Data for Name: purchase_order_item; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.purchase_order_item (id, amount_before_tax, cost_before_tax, notes, quantity, received_quantity, tax_amount, total_amount, unit, unit_price, product_id, purchase_order_id) FROM stdin;
1	42500000.00	8500000.00	\N	5	5	4250000.00	46750000.00	Cái	8500000.00	1	1
2	22500000.00	450000.00	\N	50	50	2250000.00	24750000.00	Cái	450000.00	2	1
3	12000000.00	1200000.00	\N	10	10	1200000.00	13200000.00	Cái	1200000.00	23	1
4	11400000.00	380000.00	\N	30	30	1140000.00	12540000.00	Cái	380000.00	24	1
5	24000000.00	12000000.00	\N	2	2	2400000.00	26400000.00	Cái	12000000.00	3	1
6	95000000.00	95000.00	\N	1000	1000	9500000.00	104500000.00	Bao	95000.00	4	2
7	90000000.00	180000.00	\N	500	500	9000000.00	99000000.00	Tấm	180000.00	5	2
8	135000000.00	45000000.00	\N	3	3	13500000.00	148500000.00	Cái	45000000.00	6	3
9	25600000.00	320000.00	\N	80	80	2560000.00	28160000.00	Cái	320000.00	7	3
10	6400000.00	320000.00	\N	20	20	640000.00	7040000.00	Cái	320000.00	25	3
11	42000000.00	280000.00	\N	150	150	4200000.00	46200000.00	Bao	280000.00	8	4
12	35000000.00	350000.00	\N	100	100	3500000.00	38500000.00	Can	350000.00	9	4
13	130000000.00	650000.00	\N	200	200	13000000.00	143000000.00	Cái	650000.00	10	5
14	120000000.00	1200000.00	\N	100	100	12000000.00	132000000.00	Cái	1200000.00	11	5
15	140000000.00	280000.00	\N	500	500	14000000.00	154000000.00	Cây	280000.00	12	6
16	111000000.00	1850000.00	\N	60	60	11100000.00	122100000.00	Tấm	1850000.00	13	6
17	78400000.00	980000.00	\N	80	80	7840000.00	86240000.00	Cây	980000.00	14	7
18	66000000.00	2200000.00	\N	30	30	6600000.00	72600000.00	Tấm	2200000.00	15	7
19	74000000.00	18500000.00	\N	4	4	7400000.00	81400000.00	Cuộn	18500000.00	16	8
20	34000000.00	850000.00	\N	40	40	3400000.00	37400000.00	Cái	850000.00	17	8
21	19000000.00	95000.00	\N	200	200	1900000.00	20900000.00	Cái	95000.00	18	9
22	22500000.00	45000.00	\N	500	500	2250000.00	24750000.00	Cái	45000.00	19	9
23	25500000.00	85000.00	\N	300	300	2550000.00	28050000.00	Cái	85000.00	20	10
24	45000000.00	4500000.00	\N	10	10	4500000.00	49500000.00	Cái	4500000.00	21	10
25	18000000.00	180000.00	\N	100	100	1800000.00	19800000.00	Cái	180000.00	22	10
26	68000000.00	8500000.00	\N	8	8	6800000.00	74800000.00	Cái	8500000.00	1	11
27	36000000.00	450000.00	\N	80	80	3600000.00	39600000.00	Cái	450000.00	2	11
28	36000000.00	12000000.00	\N	3	3	3600000.00	39600000.00	Cái	12000000.00	3	11
29	18000000.00	1200000.00	\N	15	15	1800000.00	19800000.00	Cái	1200000.00	23	11
30	196000000.00	280000.00	\N	700	700	19600000.00	215600000.00	Cây	280000.00	12	12
31	148000000.00	1850000.00	\N	80	80	14800000.00	162800000.00	Tấm	1850000.00	13	12
32	56000000.00	280000.00	\N	200	200	5600000.00	61600000.00	Bao	280000.00	8	13
33	52500000.00	350000.00	\N	150	150	5250000.00	57750000.00	Can	350000.00	9	13
34	114000000.00	95000.00	\N	1200	1200	11400000.00	125400000.00	Bao	95000.00	4	14
35	108000000.00	180000.00	\N	600	600	10800000.00	118800000.00	Tấm	180000.00	5	14
36	162500000.00	650000.00	\N	250	250	16250000.00	178750000.00	Cái	650000.00	10	15
37	144000000.00	1200000.00	\N	120	120	0.00	144000000.00	Cái	1200000.00	11	15
38	98000000.00	980000.00	\N	100	100	9800000.00	107800000.00	Cây	980000.00	14	16
39	77000000.00	2200000.00	\N	35	35	7700000.00	84700000.00	Tấm	2200000.00	15	16
40	92500000.00	18500000.00	\N	5	5	9250000.00	101750000.00	Cuộn	18500000.00	16	17
41	51000000.00	850000.00	\N	60	60	5100000.00	56100000.00	Cái	850000.00	17	17
42	42500000.00	85000.00	\N	500	500	4250000.00	46750000.00	Cái	85000.00	20	18
43	67500000.00	4500000.00	\N	15	15	6750000.00	74250000.00	Cái	4500000.00	21	18
44	27000000.00	180000.00	\N	150	150	2700000.00	29700000.00	Cái	180000.00	22	18
45	28500000.00	95000.00	\N	300	300	2850000.00	31350000.00	Cái	95000.00	18	19
46	27000000.00	45000.00	\N	600	600	2700000.00	29700000.00	Cái	45000.00	19	19
47	180000000.00	45000000.00	\N	4	4	18000000.00	198000000.00	Cái	45000000.00	6	20
48	16000000.00	320000.00	\N	50	50	1600000.00	17600000.00	Cái	320000.00	7	20
49	85000000.00	8500000.00	\N	10	10	8500000.00	93500000.00	Cái	8500000.00	1	21
50	45000000.00	450000.00	\N	100	100	4500000.00	49500000.00	Cái	450000.00	2	21
51	19000000.00	380000.00	\N	50	50	1900000.00	20900000.00	Cái	380000.00	24	21
52	280000000.00	280000.00	\N	1000	0	28000000.00	308000000.00	Cây	280000.00	12	22
53	185000000.00	1850000.00	\N	100	0	18500000.00	203500000.00	Tấm	1850000.00	13	22
54	70000000.00	280000.00	\N	250	0	7000000.00	77000000.00	Bao	280000.00	8	23
55	70000000.00	350000.00	\N	200	0	7000000.00	77000000.00	Can	350000.00	9	23
56	117600000.00	980000.00	\N	120	0	11760000.00	129360000.00	Cây	980000.00	14	24
57	88000000.00	2200000.00	\N	40	0	8800000.00	96800000.00	Tấm	2200000.00	15	24
58	142500000.00	95000.00	\N	1500	0	14250000.00	156750000.00	Bao	95000.00	4	25
59	144000000.00	180000.00	\N	800	0	14400000.00	158400000.00	Tấm	180000.00	5	25
60	195000000.00	650000.00	\N	300	0	19500000.00	214500000.00	Cái	650000.00	10	26
61	180000000.00	1200000.00	\N	150	0	0.00	180000000.00	Cái	1200000.00	11	26
64	38000000.00	95000.00	\N	400	0	3800000.00	41800000.00	Cái	95000.00	18	28
65	36000000.00	45000.00	\N	800	0	3600000.00	39600000.00	Cái	45000.00	19	28
66	51000000.00	85000.00	\N	600	0	5100000.00	56100000.00	Cái	85000.00	20	29
67	90000000.00	4500000.00	\N	20	0	9000000.00	99000000.00	Cái	4500000.00	21	29
68	36000000.00	180000.00	\N	200	0	3600000.00	39600000.00	Cái	180000.00	22	29
69	225000000.00	45000000.00	\N	5	0	22500000.00	247500000.00	Cái	45000000.00	6	30
70	19200000.00	320000.00	\N	60	0	1920000.00	21120000.00	Cái	320000.00	7	30
62	111000000.00	18500000.00	\N	6	5	11100000.00	122100000.00	Cuộn	18500000.00	16	27
63	59500000.00	850000.00	\N	70	65	5950000.00	65450000.00	Cái	850000.00	17	27
71	85000000.00	85000000.00	\N	10	0	\N	85000000.00		8500000.00	1	32
72	85000000.00	85000000.00	\N	10	5	\N	85000000.00		8500000.00	1	33
73	8500000.00	8500000.00	\N	1	1	\N	8500000.00		8500000.00	1	34
74	42500000.00	42500000.00	\N	5	3	\N	42500000.00		8500000.00	1	35
75	51000000.00	51000000.00	\N	6	4	\N	51000000.00		8500000.00	1	36
76	850000000.00	850000000.00	\N	100	0	\N	850000000.00		8500000.00	1	37
77	1350000.00	1350000.00	\N	3	0	\N	1350000.00		450000.00	2	38
\.


--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.roles (id, description, name) FROM stdin;
7	System Administrator - Full access to all operations	ROLE_ADMIN
8	Purchasing Staff - Create and manage purchase orders	ROLE_PURCHASE_STAFF
9	Purchasing Manager - Manage purchases and approve purchase orders	ROLE_PURCHASE_MANAGER
10	Sales Staff - Create and manage sales orders	ROLE_SALES_STAFF
11	Sales Manager - Manage sales and approve sales orders	ROLE_SALES_MANAGER
12	Warehouse Staff - Manage goods receipt/issue and inventory	ROLE_WAREHOUSE_STAFF
13	Delivery Administrator - Manage delivery plans and assign shippers	ROLE_DELIVERY_ADMIN
14	Shipper - Handle assigned delivery trips only	ROLE_SHIPPER
15	Accountant - Approve orders and view financial data	ROLE_ACCOUNTANT
17	Purchasing Staff	ROLE_PURCHASING_STAFF
18	Purchasing Manager	ROLE_PURCHASING_MANAGER
21	Supplier	ROLE_SUPPLIER
24	Customer	ROLE_CUSTOMER
\.


--
-- Data for Name: sales_invoice; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sales_invoice (id, code, created_at, created_by, discount_amount, due_date, invoice_date, issued_by, issued_date, notes, paid_amount, paid_date, payment_method, payment_reference, remaining_amount, shipping_cost, status, subtotal, tax_amount, total_amount, updated_at, customer_id, goods_issue_id, sales_order_id) FROM stdin;
1	INV-2026-001	2026-03-28 18:26:55.175667	10	\N	2026-02-21	2026-01-22	15	2026-01-22 15:00:00	Thanh toán đúng hạn	94050000.00	2026-01-27 15:00:00	Chuyển khoản	\N	0.00	\N	PAID	85500000.00	8550000.00	94050000.00	\N	1	1	1
2	INV-2026-002	2026-03-28 18:26:55.175667	10	\N	2026-02-22	2026-01-23	15	2026-01-23 16:00:00	Thanh toán đúng hạn	198000000.00	2026-01-28 16:00:00	Chuyển khoản	\N	0.00	\N	PAID	180000000.00	18000000.00	198000000.00	\N	5	2	2
3	INV-2026-003	2026-03-28 18:26:55.175667	10	\N	2026-02-25	2026-01-26	15	2026-01-26 14:00:00	Thanh toán đúng hạn	159500000.00	2026-01-31 14:00:00	Chuyển khoản	\N	0.00	\N	PAID	145000000.00	14500000.00	159500000.00	\N	7	3	3
4	INV-2026-004	2026-03-28 18:26:55.175667	10	\N	2026-02-26	2026-01-27	15	2026-01-27 16:00:00	Thanh toán đúng hạn	101200000.00	2026-02-01 16:00:00	Chuyển khoản	\N	0.00	\N	PAID	92000000.00	9200000.00	101200000.00	\N	4	4	4
5	INV-2026-005	2026-03-28 18:26:55.175667	10	\N	2026-02-27	2026-01-28	15	2026-01-28 17:00:00	Thanh toán đúng hạn	162800000.00	2026-02-02 17:00:00	Chuyển khoản	\N	0.00	\N	PAID	148000000.00	14800000.00	162800000.00	\N	6	5	5
6	INV-2026-006	2026-03-28 18:26:55.175667	10	\N	2026-02-28	2026-01-29	15	2026-01-29 16:00:00	Thanh toán đúng hạn	181500000.00	2026-02-03 16:00:00	Chuyển khoản	\N	0.00	\N	PAID	165000000.00	16500000.00	181500000.00	\N	3	6	6
7	INV-2026-007	2026-03-28 18:26:55.175667	10	\N	2026-03-01	2026-01-30	15	2026-01-30 15:00:00	Thanh toán đúng hạn	107800000.00	2026-02-04 15:00:00	Chuyển khoản	\N	0.00	\N	PAID	98000000.00	9800000.00	107800000.00	\N	11	7	7
8	INV-2026-008	2026-03-28 18:26:55.175667	10	\N	2026-03-02	2026-01-31	15	2026-01-31 17:00:00	Thanh toán đúng hạn	242000000.00	2026-02-05 17:00:00	Chuyển khoản	\N	0.00	\N	PAID	220000000.00	22000000.00	242000000.00	\N	13	8	8
9	INV-2026-009	2026-03-28 18:26:55.175667	10	\N	2026-03-04	2026-02-02	15	2026-02-02 15:00:00	Thanh toán đúng hạn	57200000.00	2026-02-07 15:00:00	Chuyển khoản	\N	0.00	\N	PAID	52000000.00	5200000.00	57200000.00	\N	9	9	9
10	INV-2026-010	2026-03-28 18:26:55.175667	10	\N	2026-03-06	2026-02-04	15	2026-02-04 16:00:00	Thanh toán đúng hạn	85800000.00	2026-02-09 16:00:00	Chuyển khoản	\N	0.00	\N	PAID	78000000.00	7800000.00	85800000.00	\N	8	10	10
11	INV-2026-011	2026-03-28 18:26:55.175667	10	\N	2026-03-23	2026-02-21	15	2026-02-21 16:00:00	Thanh toán đúng hạn	132000000.00	2026-02-26 16:00:00	Chuyển khoản	\N	0.00	\N	PAID	120000000.00	12000000.00	132000000.00	\N	5	11	11
12	INV-2026-012	2026-03-28 18:26:55.175667	10	\N	2026-03-26	2026-02-24	15	2026-02-24 14:00:00	Thanh toán đúng hạn	217800000.00	2026-03-01 14:00:00	Chuyển khoản	\N	0.00	\N	PAID	198000000.00	19800000.00	217800000.00	\N	3	12	12
13	INV-2026-013	2026-03-28 18:26:55.175667	10	\N	2026-03-28	2026-02-26	15	2026-02-26 15:00:00	Thanh toán đúng hạn	290400000.00	2026-03-03 15:00:00	Chuyển khoản	\N	0.00	\N	PAID	264000000.00	26400000.00	290400000.00	\N	13	13	13
14	INV-2026-014	2026-03-28 18:26:55.175667	10	\N	2026-03-29	2026-02-27	15	2026-02-27 16:00:00	Thanh toán đúng hạn	104500000.00	2026-03-04 16:00:00	Chuyển khoản	\N	0.00	\N	PAID	95000000.00	9500000.00	104500000.00	\N	8	14	14
15	INV-2026-015	2026-03-28 18:26:55.175667	10	\N	2026-03-31	2026-03-01	15	2026-03-01 15:00:00	Thanh toán đúng hạn	82500000.00	2026-03-06 15:00:00	Chuyển khoản	\N	0.00	\N	PAID	75000000.00	7500000.00	82500000.00	\N	6	15	15
16	INV-2026-016	2026-03-28 18:26:55.175667	10	\N	2026-04-01	2026-03-02	15	2026-03-02 14:00:00	Thanh toán đúng hạn	143000000.00	2026-03-07 14:00:00	Chuyển khoản	\N	0.00	\N	PAID	130000000.00	13000000.00	143000000.00	\N	7	16	16
17	INV-2026-017	2026-03-28 18:26:55.175667	10	\N	2026-04-02	2026-03-03	15	2026-03-03 16:00:00	Thanh toán đúng hạn	121000000.00	2026-03-08 16:00:00	Chuyển khoản	\N	0.00	\N	PAID	110000000.00	11000000.00	121000000.00	\N	4	17	17
18	INV-2026-018	2026-03-28 18:26:55.175667	10	\N	2026-04-04	2026-03-05	15	2026-03-05 15:00:00	Thanh toán đúng hạn	96800000.00	2026-03-10 15:00:00	Chuyển khoản	\N	0.00	\N	PAID	88000000.00	8800000.00	96800000.00	\N	1	18	18
19	INV-2026-019	2026-03-28 18:26:55.175667	10	\N	2026-04-06	2026-03-07	15	2026-03-07 16:00:00	Thanh toán đúng hạn	247500000.00	2026-03-12 16:00:00	Chuyển khoản	\N	0.00	\N	PAID	225000000.00	22500000.00	247500000.00	\N	12	19	19
20	INV-2026-020	2026-03-28 18:26:55.175667	10	\N	2026-04-08	2026-03-09	15	2026-03-09 14:00:00	Thanh toán đúng hạn	71500000.00	2026-03-14 14:00:00	Chuyển khoản	\N	0.00	\N	PAID	65000000.00	6500000.00	71500000.00	\N	14	20	20
21	INV-2026-021	2026-03-28 18:26:55.175667	10	\N	2026-04-20	2026-03-21	15	2026-03-21 16:00:00	Thanh toán đúng hạn	220000000.00	2026-03-26 16:00:00	Chuyển khoản	\N	0.00	\N	PAID	200000000.00	20000000.00	220000000.00	\N	5	21	21
22	INV-1777631794724	2026-05-01 17:36:34.726092	\N	\N	2026-05-31	2026-05-01	\N	\N	\N	0.00	\N	\N	\N	59500000.00	\N	DRAFT	59500000.00	\N	59500000.00	2026-05-01 17:36:34.726092	1	22	38
\.


--
-- Data for Name: sales_invoice_item; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sales_invoice_item (id, amount_before_tax, description, discount_percent, quantity, tax_amount, tax_percent, total_amount, unit, unit_price, goods_issue_item_id, product_id, sales_invoice_id) FROM stdin;
1	28500000.00	Board mạch điều khiển PLC S7-300	\N	3	2850000.00	10.00	31350000.00	Cái	9500000.00	1	1	1
2	15000000.00	Cảm biến nhiệt độ PT100	\N	30	1500000.00	10.00	16500000.00	Cái	500000.00	2	2	1
3	10800000.00	Encoder Autonics E40S6	\N	8	1080000.00	10.00	11880000.00	Cái	1350000.00	3	23	1
4	8400000.00	Relay nhiệt LS MT-32 28-40A	\N	20	840000.00	10.00	9240000.00	Cái	420000.00	4	24	1
5	47500000.00	Board mạch điều khiển PLC S7-300	\N	5	4750000.00	10.00	52250000.00	Cái	9500000.00	5	1	2
6	40500000.00	Biến tần ABB ACS550 5.5kW	\N	3	4050000.00	10.00	44550000.00	Cái	13500000.00	6	3	2
7	25000000.00	Cảm biến nhiệt độ PT100	\N	50	2500000.00	10.00	27500000.00	Cái	500000.00	7	2	2
8	64000000.00	Thép hộp 50x50x2mm	\N	200	6400000.00	10.00	70400000.00	Cây	320000.00	8	12	3
9	52500000.00	Thép tấm SS400 3mm	\N	25	5250000.00	10.00	57750000.00	Tấm	2100000.00	9	13	3
10	22000000.00	Ống inox 304 phi 76 x 2mm	\N	20	2200000.00	10.00	24200000.00	Cây	1100000.00	10	14	3
11	31000000.00	Hóa chất xử lý nước NaOH 99%	\N	100	3100000.00	10.00	34100000.00	Bao	310000.00	11	8	4
12	36000000.00	Thùng nhựa HDPE 200L	\N	50	3600000.00	10.00	39600000.00	Cái	720000.00	12	10	4
13	19500000.00	Acid Sulfuric 98% công nghiệp	\N	50	1950000.00	10.00	21450000.00	Can	390000.00	13	9	4
14	100000000.00	Máy bơm công nghiệp GRUNDFOS 15kW	\N	2	10000000.00	10.00	110000000.00	Cái	50000000.00	14	6	5
15	21600000.00	Van bi inox 304 DN50	\N	60	2160000.00	10.00	23760000.00	Cái	360000.00	15	7	5
16	5400000.00	Đồng hồ áp suất Wise P110 0-10 bar	\N	15	540000.00	10.00	5940000.00	Cái	360000.00	16	25	5
17	84000000.00	Xi măng Hà Tiên 50kg	\N	800	8400000.00	10.00	92400000.00	Bao	105000.00	17	4	6
18	80000000.00	Gạch ceramic 60x60 cao cấp	\N	400	8000000.00	10.00	88000000.00	Tấm	200000.00	18	5	6
19	19000000.00	Vòng bi SKF 6205-2RS	\N	200	1900000.00	10.00	20900000.00	Cái	95000.00	19	20	7
20	15750000.00	Dây curoa cao su Type B70	\N	150	1575000.00	10.00	17325000.00	Cái	105000.00	20	18	7
21	38400000.00	Thép hộp 50x50x2mm	\N	120	3840000.00	10.00	42240000.00	Cây	320000.00	21	12	7
22	21000000.00	Thép tấm SS400 3mm	\N	10	2100000.00	10.00	23100000.00	Tấm	2100000.00	22	13	7
23	128000000.00	Thép hộp 50x50x2mm	\N	400	12800000.00	10.00	140800000.00	Cây	320000.00	23	12	8
24	84000000.00	Thép tấm SS400 3mm	\N	40	8400000.00	10.00	92400000.00	Tấm	2100000.00	24	13	8
25	15750000.00	Dây curoa cao su Type B70	\N	150	1575000.00	10.00	17325000.00	Cái	105000.00	25	18	9
26	9500000.00	Vòng bi SKF 6205-2RS	\N	100	950000.00	10.00	10450000.00	Cái	95000.00	26	20	9
27	12600000.00	Relay nhiệt LS MT-32 28-40A	\N	30	1260000.00	10.00	13860000.00	Cái	420000.00	27	24	9
28	3600000.00	Đồng hồ áp suất Wise P110 0-10 bar	\N	10	360000.00	10.00	3960000.00	Cái	360000.00	28	25	9
29	41000000.00	Cáp điện CADIVI 3x50mm2	\N	2	4100000.00	10.00	45100000.00	Cuộn	20500000.00	29	16	10
30	23750000.00	Aptomat MCB CHINT 3P 100A	\N	25	2375000.00	10.00	26125000.00	Cái	950000.00	30	17	10
31	2880000.00	Đồng hồ áp suất Wise P110 0-10 bar	\N	8	288000.00	10.00	3168000.00	Cái	360000.00	31	25	10
32	38000000.00	Board mạch điều khiển PLC S7-300	\N	4	3800000.00	10.00	41800000.00	Cái	9500000.00	32	1	11
33	27000000.00	Biến tần ABB ACS550 5.5kW	\N	2	2700000.00	10.00	29700000.00	Cái	13500000.00	33	3	11
34	13500000.00	Encoder Autonics E40S6	\N	10	1350000.00	10.00	14850000.00	Cái	1350000.00	34	23	11
35	105000000.00	Xi măng Hà Tiên 50kg	\N	1000	10500000.00	10.00	115500000.00	Bao	105000.00	35	4	12
36	100000000.00	Gạch ceramic 60x60 cao cấp	\N	500	10000000.00	10.00	110000000.00	Tấm	200000.00	36	5	12
37	144000000.00	Thép hộp 50x50x2mm	\N	450	14400000.00	10.00	158400000.00	Cây	320000.00	37	12	13
38	105000000.00	Thép tấm SS400 3mm	\N	50	10500000.00	10.00	115500000.00	Tấm	2100000.00	38	13	13
39	61500000.00	Cáp điện CADIVI 3x50mm2	\N	3	6150000.00	10.00	67650000.00	Cuộn	20500000.00	39	16	14
40	28500000.00	Aptomat MCB CHINT 3P 100A	\N	30	2850000.00	10.00	31350000.00	Cái	950000.00	40	17	14
41	37200000.00	Hóa chất xử lý nước NaOH 99%	\N	120	3720000.00	10.00	40920000.00	Bao	310000.00	41	8	15
42	31200000.00	Acid Sulfuric 98% công nghiệp	\N	80	3120000.00	10.00	34320000.00	Can	390000.00	42	9	15
43	66000000.00	Ống inox 304 phi 76 x 2mm	\N	60	6600000.00	10.00	72600000.00	Cây	1100000.00	43	14	16
44	62500000.00	Cảm biến nhiệt độ PT100	\N	25	6250000.00	10.00	68750000.00	Tấm	2500000.00	44	2	16
45	57600000.00	Thùng nhựa HDPE 200L	\N	80	5760000.00	10.00	63360000.00	Cái	720000.00	45	10	17
46	21600000.00	Van bi inox 304 DN50	\N	60	2160000.00	10.00	23760000.00	Cái	360000.00	46	7	17
47	28500000.00	Vòng bi SKF 6205-2RS	\N	300	2850000.00	10.00	31350000.00	Cái	95000.00	47	20	18
48	21000000.00	Dây curoa cao su Type B70	\N	200	2100000.00	10.00	23100000.00	Cái	105000.00	48	18	18
49	12600000.00	Relay nhiệt LS MT-32 28-40A	\N	30	1260000.00	10.00	13860000.00	Cái	420000.00	49	24	18
50	3600000.00	Đồng hồ áp suất Wise P110 0-10 bar	\N	10	360000.00	10.00	3960000.00	Cái	360000.00	50	25	18
51	150000000.00	Máy bơm công nghiệp GRUNDFOS 15kW	\N	3	15000000.00	10.00	165000000.00	Cái	50000000.00	51	6	19
52	18000000.00	Van bi inox 304 DN50	\N	50	1800000.00	10.00	19800000.00	Cái	360000.00	52	7	19
53	3600000.00	Đồng hồ áp suất Wise P110 0-10 bar	\N	10	360000.00	10.00	3960000.00	Cái	360000.00	53	25	19
54	36000000.00	Thùng nhựa HDPE 200L	\N	50	3600000.00	10.00	39600000.00	Cái	720000.00	54	10	20
55	18600000.00	Hóa chất xử lý nước NaOH 99%	\N	60	1860000.00	10.00	20460000.00	Bao	310000.00	55	8	20
56	57000000.00	Board mạch điều khiển PLC S7-300	\N	6	5700000.00	10.00	62700000.00	Cái	9500000.00	56	1	21
57	54000000.00	Biến tần ABB ACS550 5.5kW	\N	4	5400000.00	10.00	59400000.00	Cái	13500000.00	57	3	21
58	30000000.00	Cảm biến nhiệt độ PT100	\N	60	3000000.00	10.00	33000000.00	Cái	500000.00	58	2	21
59	59500000.00	Board mạch điều khiển PLC S7-300	\N	7	0.00	\N	59500000.00	\N	8500000.00	59	1	22
\.


--
-- Data for Name: sales_order; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sales_order (id, approved_by, approved_date, code, completed_date, created_at, created_by, discount_amount, expected_delivery_date, grand_total, notes, order_date, order_name, payment_status, rejection_reason, shipping_cost, status, tax_amount, total_amount, updated_at, customer_id, delivery_address_id, warehouse_id) FROM stdin;
1	10	2026-01-06 10:00:00	SO-2026-001	2026-01-22 15:00:00	2026-03-28 18:26:55.169645	9	\N	2026-01-20	94050000.00	\N	2026-01-05	Cung cấp linh kiện Q1 lần 1	PAID	\N	\N	ORDER_COMPLETED	8550000.00	85500000.00	\N	1	1	1
2	10	2026-01-07 09:00:00	SO-2026-002	2026-01-23 16:00:00	2026-03-28 18:26:55.169645	9	\N	2026-01-22	198000000.00	\N	2026-01-06	PLC & Biến tần batch 1	PAID	\N	\N	ORDER_COMPLETED	18000000.00	180000000.00	\N	5	7	1
3	10	2026-01-08 10:30:00	SO-2026-003	2026-01-26 14:00:00	2026-03-28 18:26:55.169645	9	\N	2026-01-25	159500000.00	\N	2026-01-07	Vật liệu thép & inox tháng 1	PAID	\N	\N	ORDER_COMPLETED	14500000.00	145000000.00	\N	7	9	1
4	10	2026-01-09 09:00:00	SO-2026-004	2026-01-27 16:00:00	2026-03-28 18:26:55.169645	9	\N	2026-01-26	101200000.00	\N	2026-01-08	Hóa chất & bao bì T1/2026	PAID	\N	\N	ORDER_COMPLETED	9200000.00	92000000.00	\N	4	6	1
5	10	2026-01-11 09:00:00	SO-2026-005	2026-01-28 17:00:00	2026-03-28 18:26:55.169645	9	\N	2026-01-28	162800000.00	\N	2026-01-10	Thiết bị bơm & van cho trạm xử lý nước	PAID	\N	\N	ORDER_COMPLETED	14800000.00	148000000.00	\N	6	8	2
6	10	2026-01-12 10:00:00	SO-2026-006	2026-01-29 16:00:00	2026-03-28 18:26:55.169645	9	\N	2026-01-29	181500000.00	\N	2026-01-11	VLXD dự án chung cư Ecopark	PAID	\N	\N	ORDER_COMPLETED	16500000.00	165000000.00	\N	3	4	1
7	10	2026-01-13 09:00:00	SO-2026-007	2026-01-30 15:00:00	2026-03-28 18:26:55.169645	9	\N	2026-01-30	107800000.00	\N	2026-01-12	Vật tư cơ khí cho dây chuyền lắp ráp	PAID	\N	\N	ORDER_COMPLETED	9800000.00	98000000.00	\N	11	13	1
8	10	2026-01-14 09:30:00	SO-2026-008	2026-01-31 17:00:00	2026-03-28 18:26:55.169645	9	\N	2026-01-31	242000000.00	\N	2026-01-13	Thép hộp & thép tấm tháng 1	PAID	\N	\N	ORDER_COMPLETED	22000000.00	220000000.00	\N	13	15	1
9	10	2026-01-15 10:00:00	SO-2026-009	2026-02-02 15:00:00	2026-03-28 18:26:55.169645	9	\N	2026-02-01	57200000.00	\N	2026-01-14	Vật tư kỹ thuật T1/2026	PAID	\N	\N	ORDER_COMPLETED	5200000.00	52000000.00	\N	9	11	2
10	10	2026-01-16 09:00:00	SO-2026-010	2026-02-04 16:00:00	2026-03-28 18:26:55.169645	9	\N	2026-02-03	85800000.00	\N	2026-01-15	Thiết bị điện & đo lường cho nhà máy dược	PAID	\N	\N	ORDER_COMPLETED	7800000.00	78000000.00	\N	8	10	1
11	10	2026-02-04 09:00:00	SO-2026-011	2026-02-21 16:00:00	2026-03-28 18:26:55.169645	9	\N	2026-02-20	132000000.00	\N	2026-02-03	Linh kiện điện tử T2 batch 1	PAID	\N	\N	ORDER_COMPLETED	12000000.00	120000000.00	\N	5	7	1
12	10	2026-02-06 10:00:00	SO-2026-012	2026-02-24 14:00:00	2026-03-28 18:26:55.169645	9	\N	2026-02-23	217800000.00	\N	2026-02-05	VLXD dự án T2/2026	PAID	\N	\N	ORDER_COMPLETED	19800000.00	198000000.00	\N	3	4	1
13	10	2026-02-08 09:00:00	SO-2026-013	2026-02-26 15:00:00	2026-03-28 18:26:55.169645	9	\N	2026-02-25	290400000.00	\N	2026-02-07	Thép kết cấu T2/2026	PAID	\N	\N	ORDER_COMPLETED	26400000.00	264000000.00	\N	13	15	1
14	10	2026-02-09 08:30:00	SO-2026-014	2026-02-27 16:00:00	2026-03-28 18:26:55.169645	9	\N	2026-02-26	104500000.00	\N	2026-02-08	Thiết bị điện T2/2026 - Hà Nội	PAID	\N	\N	ORDER_COMPLETED	9500000.00	95000000.00	\N	8	10	1
15	10	2026-02-11 09:00:00	SO-2026-015	2026-03-01 15:00:00	2026-03-28 18:26:55.169645	9	\N	2026-02-28	82500000.00	\N	2026-02-10	Hóa chất xử lý nước T2/2026	PAID	\N	\N	ORDER_COMPLETED	7500000.00	75000000.00	\N	6	8	1
16	10	2026-02-12 10:00:00	SO-2026-016	2026-03-02 14:00:00	2026-03-28 18:26:55.169645	9	\N	2026-03-01	143000000.00	\N	2026-02-11	Inox & kim loại T2/2026	PAID	\N	\N	ORDER_COMPLETED	13000000.00	130000000.00	\N	7	9	1
17	10	2026-02-13 09:00:00	SO-2026-017	2026-03-03 16:00:00	2026-03-28 18:26:55.169645	9	\N	2026-03-02	121000000.00	\N	2026-02-12	Bao bì & pallet T2/2026	PAID	\N	\N	ORDER_COMPLETED	11000000.00	110000000.00	\N	4	6	2
18	10	2026-02-15 09:30:00	SO-2026-018	2026-03-05 15:00:00	2026-03-28 18:26:55.169645	9	\N	2026-03-04	96800000.00	\N	2026-02-14	Linh kiện cơ khí T2/2026	PAID	\N	\N	ORDER_COMPLETED	8800000.00	88000000.00	\N	1	1	1
19	10	2026-02-17 10:00:00	SO-2026-019	2026-03-07 16:00:00	2026-03-28 18:26:55.169645	9	\N	2026-03-06	247500000.00	\N	2026-02-16	Thiết bị bơm T2/2026	PAID	\N	\N	ORDER_COMPLETED	22500000.00	225000000.00	\N	12	14	2
20	10	2026-02-19 09:00:00	SO-2026-020	2026-03-09 14:00:00	2026-03-28 18:26:55.169645	9	\N	2026-03-08	71500000.00	\N	2026-02-18	Nhựa Đà Nẵng - vật tư T2/2026	PAID	\N	\N	ORDER_COMPLETED	6500000.00	65000000.00	\N	14	16	2
21	10	2026-03-04 09:00:00	SO-2026-021	2026-03-21 16:00:00	2026-03-28 18:26:55.169645	9	\N	2026-03-20	220000000.00	\N	2026-03-03	Samsung - PLC T3/2026	PAID	\N	\N	ORDER_COMPLETED	20000000.00	200000000.00	\N	5	7	1
22	10	2026-03-06 10:00:00	SO-2026-022	\N	2026-03-28 18:26:55.169645	9	\N	2026-03-25	308000000.00	\N	2026-03-05	Thép T3/2026 - Hòa Phát	UNPAID	\N	\N	ORDER_APPROVED	28000000.00	280000000.00	\N	13	15	1
23	10	2026-03-07 09:00:00	SO-2026-023	\N	2026-03-28 18:26:55.169645	9	\N	2026-03-28	242000000.00	\N	2026-03-06	VLXD T3/2026 - Xây dựng số 1	UNPAID	\N	\N	ORDER_APPROVED	22000000.00	220000000.00	\N	3	4	1
24	10	2026-03-08 09:00:00	SO-2026-024	\N	2026-03-28 18:26:55.169645	9	\N	2026-03-28	195800000.00	\N	2026-03-07	Thiết bị xử lý nước T3/2026	UNPAID	\N	\N	ORDER_APPROVED	17800000.00	178000000.00	\N	6	8	2
25	10	2026-03-09 10:00:00	SO-2026-025	\N	2026-03-28 18:26:55.169645	9	\N	2026-03-28	170500000.00	\N	2026-03-08	Inox T3/2026 - Cơ khí Tân Thành	UNPAID	\N	\N	ORDER_APPROVED	15500000.00	155000000.00	\N	7	9	1
26	10	2026-03-10 09:00:00	SO-2026-026	\N	2026-03-28 18:26:55.169645	9	\N	2026-03-29	112750000.00	\N	2026-03-09	Dây điện T3/2026 - Long Hậu	UNPAID	\N	\N	ORDER_APPROVED	10250000.00	102500000.00	\N	15	17	1
27	10	2026-03-11 09:30:00	SO-2026-027	\N	2026-03-28 18:26:55.169645	9	\N	2026-03-28	129800000.00	\N	2026-03-10	Vật tư THACO T3/2026	UNPAID	\N	\N	ORDER_APPROVED	11800000.00	118000000.00	\N	11	13	1
28	\N	\N	SO-2026-028	\N	2026-03-28 18:26:55.169645	9	\N	2026-04-01	104500000.00	\N	2026-03-12	Bao bì giấy T3/2026	UNPAID	\N	\N	ORDER_OPEN	9500000.00	95000000.00	\N	10	12	1
29	\N	\N	SO-2026-029	\N	2026-03-28 18:26:55.169645	9	\N	2026-04-02	93500000.00	\N	2026-03-13	Imexpharm dược - thiết bị T3/2026	UNPAID	\N	\N	ORDER_OPEN	8500000.00	85000000.00	\N	8	10	1
30	\N	\N	SO-2026-030	\N	2026-03-28 18:26:55.169645	9	\N	2026-04-03	79200000.00	\N	2026-03-14	Vật tư tổng hợp Nhựa ĐN T3/2026	UNPAID	\N	\N	ORDER_OPEN	7200000.00	72000000.00	\N	2	3	2
34	\N	2026-05-01 17:11:05.246496	SO-1777630257255	\N	2026-05-01 17:10:57.261434	\N	0.00	2026-05-02	9350000.00		2026-05-01		UNPAID	\N	0.00	ORDER_APPROVED	850000.00	9350000.00	2026-05-01 17:11:05.252151	8	10	1
31	\N	\N	SO-1774707482512	\N	2026-03-28 21:18:02.518513	\N	0.00	2026-03-29	6600000.00		2026-03-28		UNPAID	\N	0.00	ORDER_OPEN	600000.00	6600000.00	2026-05-29 06:49:25.919478	1	18	2
32	\N	2026-05-29 06:49:46.665582	SO-1775271186725	\N	2026-04-04 09:53:06.735378	\N	0.00	2026-04-05	13200000.00		2026-04-04	Đơn hàng 01	UNPAID	\N	0.00	ORDER_APPROVED	1200000.00	13200000.00	2026-05-29 06:49:46.666731	10	12	1
35	\N	2026-05-01 17:14:21.182807	SO-1777630452421	\N	2026-05-01 17:14:12.427601	\N	0.00	\N	9350000.00		2026-05-01		UNPAID	kh	0.00	ORDER_CANCELLED	850000.00	9350000.00	2026-05-01 17:22:50.406322	1	18	1
38	\N	2026-05-01 17:27:53.267986	SO-1777631267242	2026-05-01 17:36:34.737281	2026-05-01 17:27:47.25014	\N	0.00	2026-05-14	65450000.00		2026-05-01		UNPAID	\N	0.00	ORDER_COMPLETED	5950000.00	65450000.00	2026-05-01 17:36:34.738075	1	18	1
36	\N	2026-05-01 17:23:10.885626	SO-1777630985862	\N	2026-05-01 17:23:05.86857	\N	0.00	2026-05-03	28050000.00		2026-05-01		UNPAID	chưa chọn kho	0.00	ORDER_CANCELLED	2550000.00	28050000.00	2026-05-01 17:26:25.59409	1	18	\N
37	\N	2026-05-01 17:26:58.125345	SO-1777631213096	\N	2026-05-01 17:26:53.102981	\N	0.00	2026-05-08	46750000.00		2026-05-01		UNPAID	\N	0.00	ORDER_APPROVED	4250000.00	46750000.00	2026-05-01 17:26:58.127213	1	18	1
33	\N	2026-05-01 17:10:25.804662	SO-1777626534788	\N	2026-05-01 16:08:54.795956	\N	0.00	2026-10-31	9350000.00		2026-05-01		UNPAID	\N	0.00	ORDER_APPROVED	850000.00	9350000.00	2026-05-01 17:10:25.807635	1	18	1
39	\N	\N	SO-1780126810850	\N	2026-05-30 14:40:10.854895	\N	0.00	2026-05-31	9350000.00		2026-05-30		UNPAID	\N	0.00	ORDER_OPEN	850000.00	9350000.00	2026-05-30 14:40:10.854903	1	18	4
40	\N	2026-05-30 14:51:40.44081	SO-1780127479354	\N	2026-05-30 14:51:19.36158	\N	0.00	2026-05-17	112200000.00		2026-05-30		UNPAID	\N	0.00	ORDER_APPROVED	10200000.00	112200000.00	2026-05-30 14:51:40.442475	8	10	1
\.


--
-- Data for Name: sales_order_item; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sales_order_item (id, amount_before_tax, delivered_quantity, discount_percent, notes, quantity, tax_amount, tax_percent, total_amount, unit, unit_price, product_id, sales_order_id) FROM stdin;
1	28500000.00	3	\N	\N	3	2850000.00	10.00	31350000.00	Cái	9500000.00	1	1
2	15000000.00	30	\N	\N	30	1500000.00	10.00	16500000.00	Cái	500000.00	2	1
3	10800000.00	8	\N	\N	8	1080000.00	10.00	11880000.00	Cái	1350000.00	23	1
4	8400000.00	20	\N	\N	20	840000.00	10.00	9240000.00	Cái	420000.00	24	1
5	47500000.00	5	\N	\N	5	4750000.00	10.00	52250000.00	Cái	9500000.00	1	2
6	40500000.00	3	\N	\N	3	4050000.00	10.00	44550000.00	Cái	13500000.00	3	2
7	25000000.00	50	\N	\N	50	2500000.00	10.00	27500000.00	Cái	500000.00	2	2
8	64000000.00	200	\N	\N	200	6400000.00	10.00	70400000.00	Cây	320000.00	12	3
9	52500000.00	25	\N	\N	25	5250000.00	10.00	57750000.00	Tấm	2100000.00	13	3
10	22000000.00	20	\N	\N	20	2200000.00	10.00	24200000.00	Cây	1100000.00	14	3
11	31000000.00	100	\N	\N	100	3100000.00	10.00	34100000.00	Bao	310000.00	8	4
12	36000000.00	50	\N	\N	50	3600000.00	10.00	39600000.00	Cái	720000.00	10	4
13	19500000.00	50	\N	\N	50	1950000.00	10.00	21450000.00	Can	390000.00	9	4
14	100000000.00	2	\N	\N	2	10000000.00	10.00	110000000.00	Cái	50000000.00	6	5
15	21600000.00	60	\N	\N	60	2160000.00	10.00	23760000.00	Cái	360000.00	7	5
16	5400000.00	15	\N	\N	15	540000.00	10.00	5940000.00	Cái	360000.00	25	5
17	84000000.00	800	\N	\N	800	8400000.00	10.00	92400000.00	Bao	105000.00	4	6
18	80000000.00	400	\N	\N	400	8000000.00	10.00	88000000.00	Tấm	200000.00	5	6
19	19000000.00	200	\N	\N	200	1900000.00	10.00	20900000.00	Cái	95000.00	20	7
20	15750000.00	150	\N	\N	150	1575000.00	10.00	17325000.00	Cái	105000.00	18	7
21	38400000.00	120	\N	\N	120	3840000.00	10.00	42240000.00	Cây	320000.00	12	7
22	21000000.00	10	\N	\N	10	2100000.00	10.00	23100000.00	Tấm	2100000.00	13	7
23	128000000.00	400	\N	\N	400	12800000.00	10.00	140800000.00	Cây	320000.00	12	8
24	84000000.00	40	\N	\N	40	8400000.00	10.00	92400000.00	Tấm	2100000.00	13	8
25	15750000.00	150	\N	\N	150	1575000.00	10.00	17325000.00	Cái	105000.00	18	9
26	9500000.00	100	\N	\N	100	950000.00	10.00	10450000.00	Cái	95000.00	20	9
27	12600000.00	30	\N	\N	30	1260000.00	10.00	13860000.00	Cái	420000.00	24	9
28	3600000.00	10	\N	\N	10	360000.00	10.00	3960000.00	Cái	360000.00	25	9
29	41000000.00	2	\N	\N	2	4100000.00	10.00	45100000.00	Cuộn	20500000.00	16	10
30	23750000.00	25	\N	\N	25	2375000.00	10.00	26125000.00	Cái	950000.00	17	10
31	2880000.00	8	\N	\N	8	288000.00	10.00	3168000.00	Cái	360000.00	25	10
32	38000000.00	4	\N	\N	4	3800000.00	10.00	41800000.00	Cái	9500000.00	1	11
33	27000000.00	2	\N	\N	2	2700000.00	10.00	29700000.00	Cái	13500000.00	3	11
34	13500000.00	10	\N	\N	10	1350000.00	10.00	14850000.00	Cái	1350000.00	23	11
35	105000000.00	1000	\N	\N	1000	10500000.00	10.00	115500000.00	Bao	105000.00	4	12
36	100000000.00	500	\N	\N	500	10000000.00	10.00	110000000.00	Tấm	200000.00	5	12
37	144000000.00	450	\N	\N	450	14400000.00	10.00	158400000.00	Cây	320000.00	12	13
38	105000000.00	50	\N	\N	50	10500000.00	10.00	115500000.00	Tấm	2100000.00	13	13
39	61500000.00	3	\N	\N	3	6150000.00	10.00	67650000.00	Cuộn	20500000.00	16	14
40	28500000.00	30	\N	\N	30	2850000.00	10.00	31350000.00	Cái	950000.00	17	14
41	37200000.00	120	\N	\N	120	3720000.00	10.00	40920000.00	Bao	310000.00	8	15
42	31200000.00	80	\N	\N	80	3120000.00	10.00	34320000.00	Can	390000.00	9	15
43	66000000.00	60	\N	\N	60	6600000.00	10.00	72600000.00	Cây	1100000.00	14	16
44	62500000.00	25	\N	\N	25	6250000.00	10.00	68750000.00	Tấm	2500000.00	2	16
45	57600000.00	80	\N	\N	80	5760000.00	10.00	63360000.00	Cái	720000.00	10	17
46	21600000.00	60	\N	\N	60	2160000.00	10.00	23760000.00	Cái	360000.00	7	17
47	28500000.00	300	\N	\N	300	2850000.00	10.00	31350000.00	Cái	95000.00	20	18
48	21000000.00	200	\N	\N	200	2100000.00	10.00	23100000.00	Cái	105000.00	18	18
49	12600000.00	30	\N	\N	30	1260000.00	10.00	13860000.00	Cái	420000.00	24	18
50	3600000.00	10	\N	\N	10	360000.00	10.00	3960000.00	Cái	360000.00	25	18
51	150000000.00	3	\N	\N	3	15000000.00	10.00	165000000.00	Cái	50000000.00	6	19
52	18000000.00	50	\N	\N	50	1800000.00	10.00	19800000.00	Cái	360000.00	7	19
53	3600000.00	10	\N	\N	10	360000.00	10.00	3960000.00	Cái	360000.00	25	19
54	36000000.00	50	\N	\N	50	3600000.00	10.00	39600000.00	Cái	720000.00	10	20
55	18600000.00	60	\N	\N	60	1860000.00	10.00	20460000.00	Bao	310000.00	8	20
56	57000000.00	6	\N	\N	6	5700000.00	10.00	62700000.00	Cái	9500000.00	1	21
57	54000000.00	4	\N	\N	4	5400000.00	10.00	59400000.00	Cái	13500000.00	3	21
58	30000000.00	60	\N	\N	60	3000000.00	10.00	33000000.00	Cái	500000.00	2	21
59	160000000.00	0	\N	\N	500	16000000.00	10.00	176000000.00	Cây	320000.00	12	22
60	126000000.00	0	\N	\N	60	12600000.00	10.00	138600000.00	Tấm	2100000.00	13	22
61	126000000.00	0	\N	\N	1200	12600000.00	10.00	138600000.00	Bao	105000.00	4	23
62	80000000.00	0	\N	\N	400	8000000.00	10.00	88000000.00	Tấm	200000.00	5	23
63	100000000.00	0	\N	\N	2	10000000.00	10.00	110000000.00	Cái	50000000.00	6	24
64	46500000.00	0	\N	\N	150	4650000.00	10.00	51150000.00	Bao	310000.00	8	24
65	11700000.00	0	\N	\N	30	1170000.00	10.00	12870000.00	Can	390000.00	9	24
66	88000000.00	0	\N	\N	80	8800000.00	10.00	96800000.00	Cây	1100000.00	14	25
67	10800000.00	0	\N	\N	30	1080000.00	10.00	11880000.00	Cái	360000.00	7	25
68	9500000.00	0	\N	\N	100	950000.00	10.00	10450000.00	Cái	95000.00	20	25
69	61500000.00	0	\N	\N	3	6150000.00	10.00	67650000.00	Cuộn	20500000.00	16	26
70	38000000.00	0	\N	\N	40	3800000.00	10.00	41800000.00	Cái	950000.00	17	26
71	23750000.00	0	\N	\N	250	2375000.00	10.00	26125000.00	Cái	95000.00	20	27
72	21000000.00	0	\N	\N	200	2100000.00	10.00	23100000.00	Cái	105000.00	18	27
73	48000000.00	0	\N	\N	150	4800000.00	10.00	52800000.00	Cây	320000.00	12	27
74	57600000.00	0	\N	\N	80	5760000.00	10.00	63360000.00	Cái	720000.00	10	28
75	24800000.00	0	\N	\N	80	2480000.00	10.00	27280000.00	Bao	310000.00	8	28
76	41000000.00	0	\N	\N	2	4100000.00	10.00	45100000.00	Cuộn	20500000.00	16	29
77	19000000.00	0	\N	\N	20	1900000.00	10.00	20900000.00	Cái	950000.00	17	29
78	4320000.00	0	\N	\N	12	432000.00	10.00	4752000.00	Cái	360000.00	25	29
79	43200000.00	0	\N	\N	60	4320000.00	10.00	47520000.00	Cái	720000.00	10	30
80	15600000.00	0	\N	\N	40	1560000.00	10.00	17160000.00	Can	390000.00	9	30
83	8500000.00	0	0.00		1	850000.00	10.00	9350000.00	\N	8500000.00	1	33
84	8500000.00	0	0.00		1	850000.00	10.00	9350000.00	\N	8500000.00	1	34
85	8500000.00	0	0.00		1	850000.00	10.00	9350000.00	\N	8500000.00	1	35
86	25500000.00	0	0.00		3	2550000.00	10.00	28050000.00	\N	8500000.00	1	36
87	42500000.00	0	0.00		5	4250000.00	10.00	46750000.00	\N	8500000.00	1	37
88	59500000.00	7	0.00		7	5950000.00	10.00	65450000.00	\N	8500000.00	1	38
89	12000000.00	0	0.00		1	1200000.00	10.00	13200000.00	\N	12000000.00	3	32
90	6000000.00	0	0.00		5	600000.00	10.00	6600000.00	\N	1200000.00	11	31
91	8500000.00	0	0.00		1	850000.00	10.00	9350000.00	\N	8500000.00	1	39
92	102000000.00	0	0.00		12	10200000.00	10.00	112200000.00	\N	8500000.00	1	40
\.


--
-- Data for Name: supplier; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.supplier (id, address, code, contact_name, email, name, phone) FROM stdin;
1	45 Đinh Tiên Hoàng, Q.1, TP.HCM	SUP001	Nguyễn Văn An	an.nguyen@linhkienmn.vn	Công ty TNHH Linh Kiện Điện Tử Miền Nam	028-3823-1100
2	123 Nguyễn Tất Thành, Q.4, TP.HCM	SUP002	Trần Thị Bích	bich.tran@hoangphat.vn	Công ty CP Vật Liệu Xây Dựng Hoàng Phát	028-3945-2200
3	88 Trần Duy Hưng, Cầu Giấy, Hà Nội	SUP003	Lê Minh Cường	cuong.le@daiviet.vn	Công ty TNHH Thiết Bị Công Nghiệp Đại Việt	024-3556-3300
4	67 Lê Đại Hành, Q.11, TP.HCM	SUP004	Phạm Thị Dung	dung.pham@hoachatsaigon.vn	Công ty CP Hóa Chất Sài Gòn	028-3721-4400
5	12 KCN Biên Hòa 2, Đồng Nai	SUP005	Hoàng Văn Em	em.hoang@tienphatapc.vn	Công ty TNHH Bao Bì Nhựa Tiến Phát	0251-382-5500
6	156 Nguyễn Văn Linh, Q.7, TP.HCM	SUP006	Vũ Thị Hoa	hoa.vu@thepmiennam.vn	Công ty CP Thép Miền Nam	028-3836-6600
7	34 Hùng Vương, Q.5, TP.HCM	SUP007	Đặng Minh Hiếu	hieu.dang@anhkim.vn	Công ty TNHH Inox & Kim Loại Ánh Kim	028-3864-7700
8	200 Trường Chinh, Đống Đa, Hà Nội	SUP008	Bùi Thị Lan	lan.bui@diencnvn.vn	Công ty CP Điện Công Nghiệp Việt Nam	024-3766-8800
9	78 KCN Mỹ Phước 3, Bình Dương	SUP009	Ngô Văn Minh	minh.ngo@caosudn.vn	Công ty TNHH Cao Su Kỹ Thuật Đông Nam	0274-382-9900
10	15 Giải Phóng, Hoàng Mai, Hà Nội	SUP010	Đinh Thị Nga	nga.dinh@linhuakien.vn	Công ty CP Linh Phụ Kiện Cơ Khí Hà Nội	024-3645-1010
\.


--
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_roles (user_id, role_id) FROM stdin;
6	7
7	8
8	9
9	10
10	11
11	12
12	13
13	14
14	14
15	15
17	17
18	18
19	12
20	15
21	10
22	11
33	10
34	12
35	8
36	9
37	10
38	11
39	13
40	14
41	15
42	10
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, active, email, full_name, password, username) FROM stdin;
17	t	john@example.com	John Buyer	$2a$10$N9qo8uLOickgx2ZMRZoMy.MqDgFl0JQYxP9W8q1w.VfANfGkqNGqO	john.buyer
18	t	jane@example.com	Jane Manager	$2a$10$N9qo8uLOickgx2ZMRZoMy.MqDgFl0JQYxP9W8q1w.VfANfGkqNGqO	jane.manager
19	t	bob@example.com	Bob Warehouse	$2a$10$N9qo8uLOickgx2ZMRZoMy.MqDgFl0JQYxP9W8q1w.VfANfGkqNGqO	bob.warehouse
20	t	alice@example.com	Alice Accountant	$2a$10$N9qo8uLOickgx2ZMRZoMy.MqDgFl0JQYxP9W8q1w.VfANfGkqNGqO	alice.accountant
21	t	sarah@example.com	Sarah Sales	$2a$10$N9qo8uLOickgx2ZMRZoMy.MqDgFl0JQYxP9W8q1w.VfANfGkqNGqO	sarah.sales
22	t	mike@example.com	Mike Manager	$2a$10$N9qo8uLOickgx2ZMRZoMy.MqDgFl0JQYxP9W8q1w.VfANfGkqNGqO	mike.manager
33	t	admin@gmail.com	ADMIN	$2a$10$QCIsPuJFVcEbLR16CHQaN.B06lmf1mNNOhpnFMRzYJ912FkB.AyIi	admin_real
34	t	WAREHOUSE_STAFF@gmail.com	WAREHOUSE_STAFF	$2a$10$62RDK4ITfVyEMlwZn8WQEe6CzA4RTc7cV5IV.CBjRI0538yrBZeL2	WAREHOUSE_STAFF
35	t	PURCHASE_STAFF@gmail.com	PURCHASE_STAFF	$2a$10$5qQyWekQLhenKQWwhzJzHe3y2TmlIu.CzG/edA/IU9Pv.d77ojkNO	PURCHASE_STAFF
36	t	PURCHASE_MANAGER@gmail.com	PURCHASE_MANAGER	$2a$10$03MdfCEaNVre/fnwPXYFeu6PB7v0ELvrpiFt1WP3cYfU7LDk1//oa	PURCHASE_MANAGER
37	t	SALES_STAFF@gmail.com	SALES_STAFF	$2a$10$vvxaTsRBbp9WrimdS8BO6ed4O9nkOeIfa8WlpljIK2kIFL4mpggae	SALES_STAFF
38	t	SALES_MANAGER@gmail.com	SALES_MANAGER	$2a$10$fqSZjM9lZga.rMx/5OQ1IOVR6TEgrrJeXSNwOcaiKtuDui1aAQ8ha	SALES_MANAGER
39	t	DELIVERY_ADMIN@gmail.com	DELIVERY_ADMIN	$2a$10$Won14qMTebJlixV./h7A2OB71pyE90WTXFUhRmJy4inYOivfYbtoO	DELIVERY_ADMIN
40	t	SHIPPER@gmail.com	SHIPPER	$2a$10$pe8QdIRrsAWPeSUMYkjpd.ggJyHcDX9cCmFMq/PCya.9rvqjnLKau	SHIPPER
41	t	ACCOUNTANT@gmail.com	ACCOUNTANT	$2a$10$I8DqLBLDIL0yT3QbrzBrAexrfTVGxwN7WdBD9MefJNsmo0zbWNYEK	ACCOUNTANT
42	t	test2@test.com	Test User	$2a$10$DR5KTgU4He92dTQl51y5Du77j93Gq.dm1fY7XkkeXMnp0HfBfQOHi	testuser2
6	t	admin@distribution.local	System Administrator	$2b$10$qVBH87sk.T2gRXfWFiX1y.8vt7QYbS/BfO9p5sP0yq3UtHbrt23KK	admin
8	t	purchase.manager@distribution.local	Purchase Manager User	$2b$10$oapRaEqjSp3FFsp6I/GqSO4I2VJijZiCn.3/FzTd38WCXJpoojiLG	purchase_manager
9	t	sales.staff@distribution.local	Sales Staff User	$2b$10$oapRaEqjSp3FFsp6I/GqSO4I2VJijZiCn.3/FzTd38WCXJpoojiLG	sales_staff
10	t	sales.manager@distribution.local	Sales Manager User	$2b$10$oapRaEqjSp3FFsp6I/GqSO4I2VJijZiCn.3/FzTd38WCXJpoojiLG	sales_manager
11	t	warehouse.staff@distribution.local	Warehouse Staff User	$2b$10$oapRaEqjSp3FFsp6I/GqSO4I2VJijZiCn.3/FzTd38WCXJpoojiLG	warehouse_staff
12	t	delivery.admin@distribution.local	Delivery Admin User	$2b$10$oapRaEqjSp3FFsp6I/GqSO4I2VJijZiCn.3/FzTd38WCXJpoojiLG	delivery_admin
13	t	shipper1@distribution.local	Shipper One	$2b$10$oapRaEqjSp3FFsp6I/GqSO4I2VJijZiCn.3/FzTd38WCXJpoojiLG	shipper1
14	t	shipper2@distribution.local	Shipper Two	$2b$10$oapRaEqjSp3FFsp6I/GqSO4I2VJijZiCn.3/FzTd38WCXJpoojiLG	shipper2
15	t	accountant@distribution.local	Accountant User	$2b$10$oapRaEqjSp3FFsp6I/GqSO4I2VJijZiCn.3/FzTd38WCXJpoojiLG	accountant
7	t	purchase.staff@distribution.local	Purchase Staff User	$2a$10$crXZHvUgGhw.bIW/DENkye1Hw/TOWsnJgPIkIip7eGYgAP9yzJr9u	purchase_staff
\.


--
-- Data for Name: warehouse; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.warehouse (id, code, description, location, name) FROM stdin;
1	WH001	Kho trung tâm, tổng diện tích 5000m2	Lô A, KCN Tân Bình, TP.HCM	Kho Tổng TP.HCM
2	WH002	Kho vệ tinh khu vực miền Nam, 2500m2	KCN VSIP II, Bình Dương	Kho Miền Nam - Bình Dương
3	WH003	Kho khu vực miền Bắc, 3000m2	KCN Thăng Long, Hà Nội	Kho Miền Bắc - Hà Nội
4	WH004	Kho khu vực miền Trung, 1500m2	KCN Hòa Khánh, Đà Nẵng	Kho Miền Trung - Đà Nẵng
5	WH005	Kho chuyên hàng xuất khẩu, 2000m2	Cảng Cát Lái, Q.2, TP.HCM	Kho Xuất Khẩu - Cảng Cát Lái
\.


--
-- Name: customer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.customer_id_seq', 35, true);


--
-- Name: delivery_address_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.delivery_address_id_seq', 37, true);


--
-- Name: delivery_order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.delivery_order_id_seq', 22, true);


--
-- Name: delivery_plan_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.delivery_plan_id_seq', 6, true);


--
-- Name: delivery_plan_order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.delivery_plan_order_id_seq', 17, true);


--
-- Name: delivery_plan_shipper_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.delivery_plan_shipper_id_seq', 9, true);


--
-- Name: delivery_triproute_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.delivery_triproute_id_seq', 7, true);


--
-- Name: delivery_triproute_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.delivery_triproute_item_id_seq', 18, true);


--
-- Name: goods_issue_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.goods_issue_id_seq', 24, true);


--
-- Name: goods_issue_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.goods_issue_item_id_seq', 61, true);


--
-- Name: goods_receipt_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.goods_receipt_id_seq', 28, true);


--
-- Name: goods_receipt_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.goods_receipt_item_id_seq', 59, true);


--
-- Name: inventory_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.inventory_id_seq', 27, true);


--
-- Name: inventory_lot_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.inventory_lot_id_seq', 8, true);


--
-- Name: inventory_reservation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.inventory_reservation_id_seq', 1, false);


--
-- Name: inventory_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.inventory_transaction_id_seq', 116, true);


--
-- Name: invoice_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.invoice_id_seq', 1, false);


--
-- Name: product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.product_id_seq', 55, true);


--
-- Name: purchase_order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.purchase_order_id_seq', 38, true);


--
-- Name: purchase_order_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.purchase_order_item_id_seq', 77, true);


--
-- Name: roles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.roles_id_seq', 33, true);


--
-- Name: sales_invoice_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.sales_invoice_id_seq', 22, true);


--
-- Name: sales_invoice_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.sales_invoice_item_id_seq', 59, true);


--
-- Name: sales_order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.sales_order_id_seq', 40, true);


--
-- Name: sales_order_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.sales_order_item_id_seq', 92, true);


--
-- Name: supplier_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.supplier_id_seq', 23, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 42, true);


--
-- Name: warehouse_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.warehouse_id_seq', 13, true);


--
-- Name: customer customer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer
    ADD CONSTRAINT customer_pkey PRIMARY KEY (id);


--
-- Name: delivery_address delivery_address_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_address
    ADD CONSTRAINT delivery_address_pkey PRIMARY KEY (id);


--
-- Name: delivery_order delivery_order_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_order
    ADD CONSTRAINT delivery_order_pkey PRIMARY KEY (id);


--
-- Name: delivery_plan_order delivery_plan_order_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_plan_order
    ADD CONSTRAINT delivery_plan_order_pkey PRIMARY KEY (id);


--
-- Name: delivery_plan delivery_plan_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_plan
    ADD CONSTRAINT delivery_plan_pkey PRIMARY KEY (id);


--
-- Name: delivery_plan_shipper delivery_plan_shipper_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_plan_shipper
    ADD CONSTRAINT delivery_plan_shipper_pkey PRIMARY KEY (id);


--
-- Name: delivery_triproute_item delivery_triproute_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_triproute_item
    ADD CONSTRAINT delivery_triproute_item_pkey PRIMARY KEY (id);


--
-- Name: delivery_triproute delivery_triproute_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_triproute
    ADD CONSTRAINT delivery_triproute_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: goods_issue_item goods_issue_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_issue_item
    ADD CONSTRAINT goods_issue_item_pkey PRIMARY KEY (id);


--
-- Name: goods_issue goods_issue_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_issue
    ADD CONSTRAINT goods_issue_pkey PRIMARY KEY (id);


--
-- Name: goods_receipt_item goods_receipt_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_receipt_item
    ADD CONSTRAINT goods_receipt_item_pkey PRIMARY KEY (id);


--
-- Name: goods_receipt goods_receipt_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_receipt
    ADD CONSTRAINT goods_receipt_pkey PRIMARY KEY (id);


--
-- Name: inventory_lot inventory_lot_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_lot
    ADD CONSTRAINT inventory_lot_pkey PRIMARY KEY (id);


--
-- Name: inventory inventory_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory
    ADD CONSTRAINT inventory_pkey PRIMARY KEY (id);


--
-- Name: inventory_reservation inventory_reservation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_reservation
    ADD CONSTRAINT inventory_reservation_pkey PRIMARY KEY (id);


--
-- Name: inventory_reservation inventory_reservation_sales_order_item_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_reservation
    ADD CONSTRAINT inventory_reservation_sales_order_item_id_key UNIQUE (sales_order_item_id);


--
-- Name: inventory_transaction inventory_transaction_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_transaction
    ADD CONSTRAINT inventory_transaction_pkey PRIMARY KEY (id);


--
-- Name: invoice invoice_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice
    ADD CONSTRAINT invoice_pkey PRIMARY KEY (id);


--
-- Name: product product_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT product_pkey PRIMARY KEY (id);


--
-- Name: purchase_order_item purchase_order_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.purchase_order_item
    ADD CONSTRAINT purchase_order_item_pkey PRIMARY KEY (id);


--
-- Name: purchase_order purchase_order_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.purchase_order
    ADD CONSTRAINT purchase_order_pkey PRIMARY KEY (id);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: sales_invoice_item sales_invoice_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_invoice_item
    ADD CONSTRAINT sales_invoice_item_pkey PRIMARY KEY (id);


--
-- Name: sales_invoice sales_invoice_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_invoice
    ADD CONSTRAINT sales_invoice_pkey PRIMARY KEY (id);


--
-- Name: sales_order_item sales_order_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_order_item
    ADD CONSTRAINT sales_order_item_pkey PRIMARY KEY (id);


--
-- Name: sales_order sales_order_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_order
    ADD CONSTRAINT sales_order_pkey PRIMARY KEY (id);


--
-- Name: supplier supplier_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.supplier
    ADD CONSTRAINT supplier_pkey PRIMARY KEY (id);


--
-- Name: delivery_triproute uk_3owna1cgho33sldjt72jlc82w; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_triproute
    ADD CONSTRAINT uk_3owna1cgho33sldjt72jlc82w UNIQUE (code);


--
-- Name: warehouse uk_9wk4ocyt0wv0hpffpr41aoweu; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.warehouse
    ADD CONSTRAINT uk_9wk4ocyt0wv0hpffpr41aoweu UNIQUE (code);


--
-- Name: goods_issue uk_hote2ivslug2o4hs9vrtol26j; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_issue
    ADD CONSTRAINT uk_hote2ivslug2o4hs9vrtol26j UNIQUE (code);


--
-- Name: sales_invoice uk_jb3m3i0hj1v684cpxega3vjse; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_invoice
    ADD CONSTRAINT uk_jb3m3i0hj1v684cpxega3vjse UNIQUE (goods_issue_id);


--
-- Name: purchase_order uk_lyhuui3e3rh2a6itktx3rwrpe; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.purchase_order
    ADD CONSTRAINT uk_lyhuui3e3rh2a6itktx3rwrpe UNIQUE (code);


--
-- Name: sales_invoice uk_o8yn10u7nk73coem89qdof6bj; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_invoice
    ADD CONSTRAINT uk_o8yn10u7nk73coem89qdof6bj UNIQUE (code);


--
-- Name: roles uk_ofx66keruapi6vyqpv6f2or37; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT uk_ofx66keruapi6vyqpv6f2or37 UNIQUE (name);


--
-- Name: sales_order uk_opmh39g2s5f63qq1o1dwfb0rx; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_order
    ADD CONSTRAINT uk_opmh39g2s5f63qq1o1dwfb0rx UNIQUE (code);


--
-- Name: product uk_product_code; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT uk_product_code UNIQUE (code);


--
-- Name: users uk_r43af9ap4edm43mmtq01oddj6; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_r43af9ap4edm43mmtq01oddj6 UNIQUE (username);


--
-- Name: customer uk_rm1bp9bhtiih5foj17t8l500j; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer
    ADD CONSTRAINT uk_rm1bp9bhtiih5foj17t8l500j UNIQUE (code);


--
-- Name: supplier uk_supplier_code; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.supplier
    ADD CONSTRAINT uk_supplier_code UNIQUE (code);


--
-- Name: goods_receipt uk_t19vrum65cj64t56tyerehe6t; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_receipt
    ADD CONSTRAINT uk_t19vrum65cj64t56tyerehe6t UNIQUE (code);


--
-- Name: inventory ukirfs573ss2fp3i3sc3xaaegfm; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory
    ADD CONSTRAINT ukirfs573ss2fp3i3sc3xaaegfm UNIQUE (product_id, warehouse_id);


--
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: warehouse warehouse_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.warehouse
    ADD CONSTRAINT warehouse_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_customer_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_customer_code ON public.customer USING btree (code);


--
-- Name: idx_customer_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_customer_email ON public.customer USING btree (email);


--
-- Name: idx_customer_name; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_customer_name ON public.customer USING btree (name);


--
-- Name: idx_da_customer; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_da_customer ON public.delivery_address USING btree (customer_id);


--
-- Name: idx_gi_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_gi_code ON public.goods_issue USING btree (code);


--
-- Name: idx_gi_issue_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_gi_issue_date ON public.goods_issue USING btree (issue_date);


--
-- Name: idx_gi_sales_order; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_gi_sales_order ON public.goods_issue USING btree (sales_order_id);


--
-- Name: idx_gi_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_gi_status ON public.goods_issue USING btree (status);


--
-- Name: idx_gii_goods_issue; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_gii_goods_issue ON public.goods_issue_item USING btree (goods_issue_id);


--
-- Name: idx_gii_product; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_gii_product ON public.goods_issue_item USING btree (product_id);


--
-- Name: idx_gii_so_item; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_gii_so_item ON public.goods_issue_item USING btree (sales_order_item_id);


--
-- Name: idx_gr_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_gr_code ON public.goods_receipt USING btree (code);


--
-- Name: idx_gr_purchase_order; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_gr_purchase_order ON public.goods_receipt USING btree (purchase_order_id);


--
-- Name: idx_gr_receipt_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_gr_receipt_date ON public.goods_receipt USING btree (receipt_date);


--
-- Name: idx_gr_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_gr_status ON public.goods_receipt USING btree (status);


--
-- Name: idx_gri_goods_receipt; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_gri_goods_receipt ON public.goods_receipt_item USING btree (goods_receipt_id);


--
-- Name: idx_gri_po_item; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_gri_po_item ON public.goods_receipt_item USING btree (purchase_order_item_id);


--
-- Name: idx_gri_product; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_gri_product ON public.goods_receipt_item USING btree (product_id);


--
-- Name: idx_inv_lot_expiry; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inv_lot_expiry ON public.inventory_lot USING btree (expiry_date);


--
-- Name: idx_inv_lot_product_warehouse; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inv_lot_product_warehouse ON public.inventory_lot USING btree (product_id, warehouse_id);


--
-- Name: idx_inv_product; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inv_product ON public.inventory USING btree (product_id);


--
-- Name: idx_inv_tx_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inv_tx_date ON public.inventory_transaction USING btree (transaction_date);


--
-- Name: idx_inv_tx_product; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inv_tx_product ON public.inventory_transaction USING btree (product_id);


--
-- Name: idx_inv_tx_ref; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inv_tx_ref ON public.inventory_transaction USING btree (reference_type, reference_id);


--
-- Name: idx_inv_tx_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inv_tx_type ON public.inventory_transaction USING btree (transaction_type);


--
-- Name: idx_inv_tx_warehouse; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inv_tx_warehouse ON public.inventory_transaction USING btree (warehouse_id);


--
-- Name: idx_inv_warehouse; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inv_warehouse ON public.inventory USING btree (warehouse_id);


--
-- Name: idx_ir_product; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ir_product ON public.inventory_reservation USING btree (product_id);


--
-- Name: idx_ir_sales_order; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ir_sales_order ON public.inventory_reservation USING btree (sales_order_id);


--
-- Name: idx_ir_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ir_status ON public.inventory_reservation USING btree (status);


--
-- Name: idx_ir_warehouse; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ir_warehouse ON public.inventory_reservation USING btree (warehouse_id);


--
-- Name: idx_po_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_po_code ON public.purchase_order USING btree (code);


--
-- Name: idx_po_created_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_po_created_date ON public.purchase_order USING btree (created_date);


--
-- Name: idx_po_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_po_status ON public.purchase_order USING btree (status);


--
-- Name: idx_po_supplier; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_po_supplier ON public.purchase_order USING btree (supplier_id);


--
-- Name: idx_poi_product; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_poi_product ON public.purchase_order_item USING btree (product_id);


--
-- Name: idx_poi_purchase_order; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_poi_purchase_order ON public.purchase_order_item USING btree (purchase_order_id);


--
-- Name: idx_si_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_si_code ON public.sales_invoice USING btree (code);


--
-- Name: idx_si_customer; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_si_customer ON public.sales_invoice USING btree (customer_id);


--
-- Name: idx_si_due_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_si_due_date ON public.sales_invoice USING btree (due_date);


--
-- Name: idx_si_goods_issue; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_si_goods_issue ON public.sales_invoice USING btree (goods_issue_id);


--
-- Name: idx_si_invoice_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_si_invoice_date ON public.sales_invoice USING btree (invoice_date);


--
-- Name: idx_si_sales_order; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_si_sales_order ON public.sales_invoice USING btree (sales_order_id);


--
-- Name: idx_si_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_si_status ON public.sales_invoice USING btree (status);


--
-- Name: idx_sii_invoice; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sii_invoice ON public.sales_invoice_item USING btree (sales_invoice_id);


--
-- Name: idx_sii_product; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sii_product ON public.sales_invoice_item USING btree (product_id);


--
-- Name: idx_so_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_so_code ON public.sales_order USING btree (code);


--
-- Name: idx_so_customer; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_so_customer ON public.sales_order USING btree (customer_id);


--
-- Name: idx_so_order_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_so_order_date ON public.sales_order USING btree (order_date);


--
-- Name: idx_so_payment_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_so_payment_status ON public.sales_order USING btree (payment_status);


--
-- Name: idx_so_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_so_status ON public.sales_order USING btree (status);


--
-- Name: idx_soi_product; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_soi_product ON public.sales_order_item USING btree (product_id);


--
-- Name: idx_soi_sales_order; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_soi_sales_order ON public.sales_order_item USING btree (sales_order_id);


--
-- Name: idx_trip_shipper; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_trip_shipper ON public.delivery_triproute USING btree (shipper_user_id);


--
-- Name: idx_trip_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_trip_status ON public.delivery_triproute USING btree (status);


--
-- Name: idx_user_roles_role_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_roles_role_id ON public.user_roles USING btree (role_id);


--
-- Name: idx_user_roles_user_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_roles_user_id ON public.user_roles USING btree (user_id);


--
-- Name: idx_users_active; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_active ON public.users USING btree (active);


--
-- Name: idx_users_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_email ON public.users USING btree (email);


--
-- Name: idx_users_username; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_username ON public.users USING btree (username);


--
-- Name: sales_order fk1dskbvyrnjye86si041brl5p9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_order
    ADD CONSTRAINT fk1dskbvyrnjye86si041brl5p9 FOREIGN KEY (delivery_address_id) REFERENCES public.delivery_address(id);


--
-- Name: sales_invoice fk1lfmce4tyolw5078oq2k8nkww; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_invoice
    ADD CONSTRAINT fk1lfmce4tyolw5078oq2k8nkww FOREIGN KEY (customer_id) REFERENCES public.customer(id);


--
-- Name: delivery_order fk1uktsujudi96ytrgxs7jw0jdu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_order
    ADD CONSTRAINT fk1uktsujudi96ytrgxs7jw0jdu FOREIGN KEY (sales_order_id) REFERENCES public.purchase_order(id);


--
-- Name: goods_receipt_item fk2172acj72x23hg1bqfc456efm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_receipt_item
    ADD CONSTRAINT fk2172acj72x23hg1bqfc456efm FOREIGN KEY (goods_receipt_id) REFERENCES public.goods_receipt(id);


--
-- Name: product fk2kxvbr72tmtscjvyp9yqb12by; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT fk2kxvbr72tmtscjvyp9yqb12by FOREIGN KEY (supplier_id) REFERENCES public.supplier(id);


--
-- Name: goods_issue fk30wqsgbh3xcq8lwd6gvqev9ig; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_issue
    ADD CONSTRAINT fk30wqsgbh3xcq8lwd6gvqev9ig FOREIGN KEY (delivery_address_id) REFERENCES public.delivery_address(id);


--
-- Name: delivery_triproute fk3usmklhmmhxaacnxfj3j99kju; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_triproute
    ADD CONSTRAINT fk3usmklhmmhxaacnxfj3j99kju FOREIGN KEY (shipper_user_id) REFERENCES public.users(id);


--
-- Name: purchase_order fk4traogu3jriq9u7e8rvm86k7i; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.purchase_order
    ADD CONSTRAINT fk4traogu3jriq9u7e8rvm86k7i FOREIGN KEY (supplier_id) REFERENCES public.supplier(id);


--
-- Name: purchase_order_item fk593lt017d995ds7nuqxgo3mmm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.purchase_order_item
    ADD CONSTRAINT fk593lt017d995ds7nuqxgo3mmm FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- Name: sales_invoice_item fk5ecm8193ymkehv9ptexxfasr5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_invoice_item
    ADD CONSTRAINT fk5ecm8193ymkehv9ptexxfasr5 FOREIGN KEY (sales_invoice_id) REFERENCES public.sales_invoice(id);


--
-- Name: delivery_triproute_item fk5edu6iyxm4j9whoig8rm5hcmx; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_triproute_item
    ADD CONSTRAINT fk5edu6iyxm4j9whoig8rm5hcmx FOREIGN KEY (triproute_id) REFERENCES public.delivery_triproute(id);


--
-- Name: inventory_lot fk64cr93hjsu44sdik07idr1i0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_lot
    ADD CONSTRAINT fk64cr93hjsu44sdik07idr1i0 FOREIGN KEY (warehouse_id) REFERENCES public.warehouse(id);


--
-- Name: inventory_lot fk7ej9vekuaepbqfjjh9ve2txi8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_lot
    ADD CONSTRAINT fk7ej9vekuaepbqfjjh9ve2txi8 FOREIGN KEY (source_receipt_id) REFERENCES public.goods_receipt(id);


--
-- Name: delivery_triproute_item fk7oebqbwe5sx3duaap7afa8wf2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_triproute_item
    ADD CONSTRAINT fk7oebqbwe5sx3duaap7afa8wf2 FOREIGN KEY (delivery_order_id) REFERENCES public.delivery_order(id);


--
-- Name: sales_order fk8hspk7dtuyxqo9l4uyl6j625p; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_order
    ADD CONSTRAINT fk8hspk7dtuyxqo9l4uyl6j625p FOREIGN KEY (warehouse_id) REFERENCES public.warehouse(id);


--
-- Name: delivery_plan_order fk91qo01qbgtvl9j4rwnbtdv0nu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_plan_order
    ADD CONSTRAINT fk91qo01qbgtvl9j4rwnbtdv0nu FOREIGN KEY (delivery_order_id) REFERENCES public.delivery_order(id);


--
-- Name: goods_receipt fk95kjjl2tnmgh2aivi02huf39p; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_receipt
    ADD CONSTRAINT fk95kjjl2tnmgh2aivi02huf39p FOREIGN KEY (warehouse_id) REFERENCES public.warehouse(id);


--
-- Name: inventory_transaction fkadysxy0xx1oug9ljtg47bkcny; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_transaction
    ADD CONSTRAINT fkadysxy0xx1oug9ljtg47bkcny FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- Name: sales_order_item fkauhjnf2u1tcyxj79su49lpqaa; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_order_item
    ADD CONSTRAINT fkauhjnf2u1tcyxj79su49lpqaa FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- Name: goods_issue_item fkbinrubmw89c3xlav5apldc361; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_issue_item
    ADD CONSTRAINT fkbinrubmw89c3xlav5apldc361 FOREIGN KEY (goods_issue_id) REFERENCES public.goods_issue(id);


--
-- Name: inventory_transaction fkdicavw4luy0qi1gm7qqwhe0e6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_transaction
    ADD CONSTRAINT fkdicavw4luy0qi1gm7qqwhe0e6 FOREIGN KEY (warehouse_id) REFERENCES public.warehouse(id);


--
-- Name: delivery_plan_order fkdv9b5b25ja56ccf3sa6p5f6jv; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_plan_order
    ADD CONSTRAINT fkdv9b5b25ja56ccf3sa6p5f6jv FOREIGN KEY (delivery_plan_id) REFERENCES public.delivery_plan(id);


--
-- Name: delivery_plan_shipper fkee6qwi7lrbs6oh53hd2g0ch9d; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_plan_shipper
    ADD CONSTRAINT fkee6qwi7lrbs6oh53hd2g0ch9d FOREIGN KEY (delivery_plan_id) REFERENCES public.delivery_plan(id);


--
-- Name: goods_issue fkek4hica5hyuld1a98juw7xxx7; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_issue
    ADD CONSTRAINT fkek4hica5hyuld1a98juw7xxx7 FOREIGN KEY (warehouse_id) REFERENCES public.warehouse(id);


--
-- Name: goods_receipt fkfe9lu9yqs6ic9kgjk99hx09g9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_receipt
    ADD CONSTRAINT fkfe9lu9yqs6ic9kgjk99hx09g9 FOREIGN KEY (purchase_order_id) REFERENCES public.purchase_order(id);


--
-- Name: sales_order_item fkfkfkjlvt4m5xfft7bys91twfn; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_order_item
    ADD CONSTRAINT fkfkfkjlvt4m5xfft7bys91twfn FOREIGN KEY (sales_order_id) REFERENCES public.sales_order(id);


--
-- Name: purchase_order fkfn0kri8xb0mi8bkb2swlnv581; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.purchase_order
    ADD CONSTRAINT fkfn0kri8xb0mi8bkb2swlnv581 FOREIGN KEY (warehouse_id) REFERENCES public.warehouse(id);


--
-- Name: user_roles fkh8ciramu9cc9q3qcqiv4ue8a6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkh8ciramu9cc9q3qcqiv4ue8a6 FOREIGN KEY (role_id) REFERENCES public.roles(id);


--
-- Name: goods_issue fkhbaqk0g1wj470cqmxoj7jg7fw; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_issue
    ADD CONSTRAINT fkhbaqk0g1wj470cqmxoj7jg7fw FOREIGN KEY (sales_order_id) REFERENCES public.sales_order(id);


--
-- Name: user_roles fkhfh9dx7w3ubf1co1vdev94g3f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkhfh9dx7w3ubf1co1vdev94g3f FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: inventory fkix9yxgetau1y25hhnv42gsiok; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory
    ADD CONSTRAINT fkix9yxgetau1y25hhnv42gsiok FOREIGN KEY (warehouse_id) REFERENCES public.warehouse(id);


--
-- Name: sales_invoice fkj8pqa921ea19ibmfytrc5p7b8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_invoice
    ADD CONSTRAINT fkj8pqa921ea19ibmfytrc5p7b8 FOREIGN KEY (goods_issue_id) REFERENCES public.goods_issue(id);


--
-- Name: sales_invoice_item fkjdmjy03q01gcm5a3q92aoh1rb; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_invoice_item
    ADD CONSTRAINT fkjdmjy03q01gcm5a3q92aoh1rb FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- Name: purchase_order_item fkmj122necubadvuquvjoq967y7; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.purchase_order_item
    ADD CONSTRAINT fkmj122necubadvuquvjoq967y7 FOREIGN KEY (purchase_order_id) REFERENCES public.purchase_order(id);


--
-- Name: inventory_lot fkmpf3g1q5kk8a01bi45x4rljvf; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_lot
    ADD CONSTRAINT fkmpf3g1q5kk8a01bi45x4rljvf FOREIGN KEY (source_receipt_item_id) REFERENCES public.goods_receipt_item(id);


--
-- Name: sales_invoice_item fknc5794a5pqf3s5v8lj30xfh6p; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_invoice_item
    ADD CONSTRAINT fknc5794a5pqf3s5v8lj30xfh6p FOREIGN KEY (goods_issue_item_id) REFERENCES public.goods_issue_item(id);


--
-- Name: goods_receipt_item fkne5pa0h7vuu4n081rfsqeckew; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_receipt_item
    ADD CONSTRAINT fkne5pa0h7vuu4n081rfsqeckew FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- Name: goods_issue_item fko37dfiypoiemt23n49gxmrxgg; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_issue_item
    ADD CONSTRAINT fko37dfiypoiemt23n49gxmrxgg FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- Name: inventory_lot fkogohaw01h3tg3a4wdqrqbwa76; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_lot
    ADD CONSTRAINT fkogohaw01h3tg3a4wdqrqbwa76 FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- Name: goods_issue_item fkoma3i9cicrcrrqwpj78dctbtc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_issue_item
    ADD CONSTRAINT fkoma3i9cicrcrrqwpj78dctbtc FOREIGN KEY (sales_order_item_id) REFERENCES public.sales_order_item(id);


--
-- Name: invoice fkowdq3uqeluk7sryl0iytpj259; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice
    ADD CONSTRAINT fkowdq3uqeluk7sryl0iytpj259 FOREIGN KEY (supplier_id) REFERENCES public.supplier(id);


--
-- Name: inventory fkp7gj4l80fx8v0uap3b2crjwp5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory
    ADD CONSTRAINT fkp7gj4l80fx8v0uap3b2crjwp5 FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- Name: invoice fkpbnhtmx9crcudpxcr5j2xjool; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice
    ADD CONSTRAINT fkpbnhtmx9crcudpxcr5j2xjool FOREIGN KEY (purchase_order_id) REFERENCES public.purchase_order(id);


--
-- Name: sales_order fkqqe3xj99rblvm5n0h0cp48gsa; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_order
    ADD CONSTRAINT fkqqe3xj99rblvm5n0h0cp48gsa FOREIGN KEY (customer_id) REFERENCES public.customer(id);


--
-- Name: delivery_address fks3kmll53uwp869rxqwtvkmcda; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_address
    ADD CONSTRAINT fks3kmll53uwp869rxqwtvkmcda FOREIGN KEY (customer_id) REFERENCES public.customer(id);


--
-- Name: sales_invoice fksopndnwecrebe8wh6dngljp4m; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_invoice
    ADD CONSTRAINT fksopndnwecrebe8wh6dngljp4m FOREIGN KEY (sales_order_id) REFERENCES public.sales_order(id);


--
-- Name: goods_receipt_item fkt5axed3n6hf7vi4rj6yegiwnb; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.goods_receipt_item
    ADD CONSTRAINT fkt5axed3n6hf7vi4rj6yegiwnb FOREIGN KEY (purchase_order_item_id) REFERENCES public.purchase_order_item(id);


--
-- Name: delivery_triproute fktdbfvt3t2is3p8n48yo7khk14; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.delivery_triproute
    ADD CONSTRAINT fktdbfvt3t2is3p8n48yo7khk14 FOREIGN KEY (delivery_plan_id) REFERENCES public.delivery_plan(id);


--
-- Name: inventory_reservation inventory_reservation_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_reservation
    ADD CONSTRAINT inventory_reservation_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- Name: inventory_reservation inventory_reservation_sales_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_reservation
    ADD CONSTRAINT inventory_reservation_sales_order_id_fkey FOREIGN KEY (sales_order_id) REFERENCES public.sales_order(id);


--
-- Name: inventory_reservation inventory_reservation_sales_order_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_reservation
    ADD CONSTRAINT inventory_reservation_sales_order_item_id_fkey FOREIGN KEY (sales_order_item_id) REFERENCES public.sales_order_item(id);


--
-- Name: inventory_reservation inventory_reservation_warehouse_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inventory_reservation
    ADD CONSTRAINT inventory_reservation_warehouse_id_fkey FOREIGN KEY (warehouse_id) REFERENCES public.warehouse(id);


--
-- PostgreSQL database dump complete
--

\unrestrict oLh7rDofNYe5EQugae4RgS5IrN6fmvG3TtQXyBSY4e3i3YxLbSUn1g0htSofXg2

