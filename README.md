# PowerManager

This is a small software which acts as a manager between a Fronius inverter and a Go-e charging box.
It utilizes the publicly available [Fronius Solar API](https://www.fronius.com/de/solarenergie/produkte/eigenheim/anlagen-monitoring/offene-schnittstellen/fronius-solar-api-json-) and the [Go-e API](https://go-e.co/app/api.pdf). The goal is to charge a connected electric car with just the available power from an inverter and to avoid using power from the grid.

## Configuration

After the first startup, the manager tries to auto-detect the Fronius inverter and the Charger by requesting values from all detected IPs. The correct answer from an IP is a hint for a hit.

However, there is a config file created which stores all available values. This file can be altered to reflect the desired behavior. 

## Modes

The manager knows four working modes: 
 - OFF: no charging, box is switched off
 - PV: Only charge with the available power
 - PV_WITH_MIN: Charge with available power but do not reduce below the minimum value
 - MAX: Charge with the maximum power