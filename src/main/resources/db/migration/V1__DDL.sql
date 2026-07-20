SET TIME ZONE 'UTC';

CREATE OR REPLACE FUNCTION trg_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- ---------------------------------------------------------------------------


CREATE TABLE locations (
                           location_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           code VARCHAR(10) NOT NULL,
                           name TEXT NOT NULL,

                           CONSTRAINT uq_locations_code UNIQUE (code)
);

COMMENT ON TABLE locations IS 'Все пункты отправления/назначения.';

-- ---------------------------------------------------------------------------

CREATE TABLE settings (
                          key VARCHAR(100) PRIMARY KEY,
                          value TEXT NOT NULL,
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
insert into settings (key,value) values ('moroshka_limit','4');
COMMENT ON TABLE settings IS 'Глобальные константы: лимиты, коэффициенты.';

-- ---------------------------------------------------------------------------

CREATE TABLE price_scale (
                             price_scale_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             km_from INT NOT NULL,
                             km_to INT NOT NULL,
                             base_price_kopecks BIGINT NOT NULL,

                             CONSTRAINT chk_price_scale_km_range CHECK (km_from <= km_to),
                             CONSTRAINT chk_price_scale_price_positive CHECK (base_price_kopecks > 0)
);

COMMENT ON TABLE price_scale IS 'Государственная шкала цен';
create index idx_price_scale_km on price_scale(km_from,km_to);
-- ---------------------------------------------------------------------------

CREATE TABLE routes (
                        route_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        depart_loc_id INT NOT NULL,
                        arrive_loc_id INT NOT NULL,
                        distance_km INT,
                        aircraft_type VARCHAR(20) not null,
                        max_price_fed_kopecks BIGINT not null,

                        CONSTRAINT fk_routes_depart_location FOREIGN KEY (depart_loc_id)
                            REFERENCES locations(location_id) ON DELETE RESTRICT,
                        CONSTRAINT fk_routes_arrive_location FOREIGN KEY (arrive_loc_id)
                            REFERENCES locations(location_id) ON DELETE RESTRICT,
                        CONSTRAINT chk_routes_different_endpoints CHECK (depart_loc_id <> arrive_loc_id),
                        constraint chk_routes_aircraft_type check (aircraft_type in ('plane','helicopter'))
);

COMMENT ON TABLE routes IS 'Все направления';
CREATE INDEX idx_routes_endpoints ON routes(depart_loc_id, arrive_loc_id);

-- ---------------------------------------------------------------------------

CREATE TABLE moroshka_tariffs (
                                  moroshka_tariff_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  route_id INT NOT NULL,
                                  adult_base_price_kopecks BIGINT NOT NULL,
                                  adult_card_price_kopecks BIGINT NOT NULL,
                                  valid_from DATE NOT NULL DEFAULT CURRENT_DATE,
                                  valid_to DATE,

                                  constraint fk_moroshka_tariffs_route foreign key (route_id)
                                      references routes(route_id) on delete restrict,
                                  CONSTRAINT chk_moroshka_tariffs_base_positive CHECK (adult_base_price_kopecks > 0),
                                  CONSTRAINT chk_moroshka_tariffs_card_positive CHECK (adult_card_price_kopecks > 0),
                                  CONSTRAINT chk_moroshka_tariffs_base_gte_card CHECK (adult_base_price_kopecks >= adult_card_price_kopecks),
                                  CONSTRAINT chk_moroshka_tariffs_valid_period CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

COMMENT ON TABLE moroshka_tariffs IS 'Тарифы Детский (50%) и багаж (1%) считаются в бэке.';
CREATE INDEX idx_moroshka_tariffs_routes ON moroshka_tariffs(route_id, valid_from DESC);

-- ---------------------------------------------------------------------------

CREATE TABLE flights (
                         flight_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         route_id INT NOT NULL,
                         flight_number VARCHAR(10) NOT NULL,
                         flight_date DATE NOT NULL,
                         departure_time TIME NOT NULL,
                         arrival_time TIME NOT NULL,
                         total_seats INT NOT NULL,
                         available_seats INT NOT NULL,

                         CONSTRAINT fk_flights_route FOREIGN KEY (route_id)
                             REFERENCES routes(route_id) ON DELETE RESTRICT,
                         CONSTRAINT chk_flights_total_seats_positive CHECK (total_seats > 0),
                         CONSTRAINT chk_flights_available_seats_non_negative CHECK (available_seats >= 0),
                         CONSTRAINT chk_flights_available_lte_total CHECK (available_seats <= total_seats)
);

COMMENT ON TABLE flights IS 'Конкретные вылеты: дата, время, остаток мест.';
CREATE INDEX idx_flights_route_date ON flights(route_id, flight_date);

-- ---------------------------------------------------------------------------

CREATE TABLE residents (
                           resident_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           last_name TEXT NOT NULL,
                           first_name TEXT NOT NULL,
                           middle_name TEXT,
                           birthdate DATE NOT NULL,
                           document_type VARCHAR(10) NOT NULL,
                           document_number VARCHAR(20) NOT NULL,
                           has_card BOOLEAN NOT NULL DEFAULT FALSE,

                           CONSTRAINT uq_residents_document UNIQUE (document_type, document_number)
);

COMMENT ON TABLE residents IS 'Реестр жителей ЯНАО.';
CREATE INDEX idx_residents_name ON residents(last_name, first_name, middle_name);

-- ---------------------------------------------------------------------------

CREATE TABLE quota_balances (
                                quota_balanc_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                resident_id INT NOT NULL,
                                year INT NOT NULL,
                                available INT NOT NULL DEFAULT 4,
                                issued INT NOT NULL DEFAULT 0,
                                refunded INT NOT NULL DEFAULT 0,
                                used  INT NOT NULL DEFAULT 0,

                                CONSTRAINT fk_quota_balances_resident FOREIGN KEY (resident_id)
                                    REFERENCES residents(resident_id) ON DELETE CASCADE,
                                CONSTRAINT uq_quota_balances_resident_year UNIQUE (resident_id, year),
                                CONSTRAINT chk_quota_balances_available_non_negative CHECK (available >= 0),
                                CONSTRAINT chk_quota_balances_issued_non_negative CHECK (issued >= 0),
                                CONSTRAINT chk_quota_balances_refunded_non_negative CHECK (refunded >= 0),
                                CONSTRAINT chk_quota_balances_used_non_negative CHECK (used >= 0)
);

COMMENT ON TABLE quota_balances IS 'Баланс лимитов Морошки по годам. Остаток = available - issued + refunded.';

-- ---------------------------------------------------------------------------

CREATE TABLE tickets (
                         ticket_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         ticket_number VARCHAR(13) NOT NULL,
                         flight_id INT NOT NULL,
                         fare_type VARCHAR(20) NOT NULL,
                         total_price_kopecks BIGINT NOT NULL,
                         status VARCHAR(20) NOT NULL DEFAULT 'issued',
                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                         updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                         CONSTRAINT fk_tickets_flight FOREIGN KEY (flight_id)
                             REFERENCES flights(flight_id) ON DELETE RESTRICT,
                         CONSTRAINT uq_tickets_number UNIQUE (ticket_number),
                         CONSTRAINT chk_tickets_fare_type CHECK (fare_type IN ('regular', 'moroshka')),
                         CONSTRAINT chk_tickets_price_non_negative CHECK (total_price_kopecks >= 0),
                         CONSTRAINT chk_tickets_status CHECK (status IN ('issued', 'refunded', 'cancelled', 'flown'))
);

COMMENT ON TABLE tickets IS 'Билеты. Цена в КОПЕЙКАХ.';
CREATE INDEX idx_tickets_flight ON tickets(flight_id);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE TRIGGER trg_tickets_updated
    BEFORE UPDATE ON tickets
    FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();

-- ---------------------------------------------------------------------------

CREATE TABLE passengers (
                            passenger_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            ticket_id INT NOT NULL,
                            resident_id INT,
                            last_name TEXT NOT NULL,
                            first_name TEXT NOT NULL,
                            middle_name TEXT,
                            birthdate DATE NOT NULL,
                            category VARCHAR(20) NOT NULL,
                            parent_passenger_id INT,
                            document_type VARCHAR(10) NOT NULL,
                            document_number VARCHAR(20) NOT NULL,

                            CONSTRAINT fk_passengers_ticket FOREIGN KEY (ticket_id)
                                REFERENCES tickets(ticket_id) ON DELETE CASCADE,
                            CONSTRAINT fk_passengers_resident FOREIGN KEY (resident_id)
                                REFERENCES residents(resident_id) ON DELETE RESTRICT,
                            CONSTRAINT fk_passengers_parent FOREIGN KEY (parent_passenger_id)
                                REFERENCES passengers(passenger_id) ON DELETE SET NULL,
                            CONSTRAINT chk_passengers_category CHECK (category IN ('adult', 'child_2_12', 'infant_no_seat', 'infant_with_seat')),
                            CONSTRAINT chk_passengers_infant_has_parent CHECK (
                                (category = 'infant_no_seat' AND parent_passenger_id IS NOT NULL) OR
                                (category <> 'infant_no_seat')
                                )
);

COMMENT ON TABLE passengers IS 'Пассажиры в билете. Младенцы связаны с родителем через parent_passenger_id.';
CREATE INDEX idx_passengers_ticket ON passengers(ticket_id);
CREATE INDEX idx_passengers_resident ON passengers(resident_id);
CREATE INDEX idx_passengers_doc ON passengers(document_type, document_number);
CREATE INDEX idx_passengers_parent ON passengers(parent_passenger_id) WHERE parent_passenger_id IS NOT NULL;

-- ---------------------------------------------------------------------------

CREATE TABLE ticket_operations (
                                   ticket_operation_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                   ticket_id INT NOT NULL,
                                   operation_type VARCHAR(20) NOT NULL,
                                   operation_dt TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                   payload JSONB,

                                   CONSTRAINT fk_operations_ticket FOREIGN KEY (ticket_id)
                                       REFERENCES tickets(ticket_id) ON DELETE CASCADE,
                                   CONSTRAINT chk_operations_type CHECK (
                                       operation_type IN ('create', 'refund', 'cancel', 'edit', 'exchange', 'use')
                                       )
);

COMMENT ON TABLE ticket_operations IS 'Журнал всех действий с билетами. payload хранит весь контекст операции.';
CREATE INDEX idx_operations_ticket ON ticket_operations(ticket_id);
CREATE INDEX idx_operations_dt ON ticket_operations(operation_dt DESC);
CREATE INDEX idx_operations_type ON ticket_operations(operation_type);
CREATE INDEX idx_operations_payload ON ticket_operations USING GIN (payload);

