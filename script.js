import http from 'k6/http';
import { sleep } from 'k6';

export default function() {
  http.get('http://43.203.118.49:8080/boards');
}
