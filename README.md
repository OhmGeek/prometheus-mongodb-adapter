# prometheus-mongodb-adapter
This is a WIP project to implement a prometheus remote storage adaptor for MongoDB, using MongoDB time series collections.

Do not use this for production (yet!).

## High Level Information:
- Requires Mongo 5.0+
- Uses VertX + Snappy + Protobuf to serve requests
- Uses a slightly modified form of the protobuf files from the main prometheus repo.
- 
