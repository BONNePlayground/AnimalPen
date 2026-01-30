# Animal Pens 2.3.0

#### Features:
- 

#### API:
- `mob_set_sheared` can now be used on any mob that has `void setSheared(boolean);` method.
- `sheep_set_sheared` deprecated
- `bucketable_pickup` can now be used with any `item` type, not only water bucket, as long as entity implements `Bucketable` interface
- `water_bucket_pickup` deprecated
- Implements ability to define custom animations for mobs in tile entity using AnimalPenMobAnimationsRegistry.
- Implements loot dropping for `non-interaction` interactions.