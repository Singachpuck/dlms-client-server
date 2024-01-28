import {Injectable} from '@angular/core';
import {Buffer} from 'buffer';

const API_ENDPOINT = 'http://localhost:8080/api/v1/';

@Injectable({
  providedIn: 'root'
})
export class UtilService {

  constructor() { }

  public encodeBase64(data: string) {
    return Buffer.from(data).toString('base64');
  }

  public decodeBase64(data: string) {
    return Buffer.from(data, 'base64').toString('binary');
  }

  public toBase64(file: File) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onloadend = () => {
        let base64String: any = reader.result;
        base64String = base64String.substring(base64String.indexOf(',') + 1);
        resolve(base64String);
      };
      reader.onerror = reject;
    })
  };

  // https://stackoverflow.com/questions/54753021/how-can-i-pass-an-auth-token-when-downloading-a-file
  public triggerDownload(content: Blob, name: string) {
    const url = URL.createObjectURL(content);

    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = name || 'file';

    anchor.click();
    URL.revokeObjectURL(url);
  }
}

export {
  API_ENDPOINT
}
