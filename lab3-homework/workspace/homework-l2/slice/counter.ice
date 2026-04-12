
#ifndef CTR_ICE
#define CTR_ICE

module Demo
{
  interface Counter
  {
    void setValue(int value);
    idempotent int getValue();
    int increment(int delta);
  };

};

#endif
