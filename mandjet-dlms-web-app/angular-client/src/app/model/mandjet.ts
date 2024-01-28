export class Mandjet {

  sensorValues: Array<Array<number>>;

  voltage: number;

  battery: number;


  constructor(sensorData?: Array<Array<number>>, voltage?: number, battery?: number) {
    this.sensorValues = sensorData || [];
    this.voltage = voltage || 0.0;
    this.battery = battery || 0;
  }
}
