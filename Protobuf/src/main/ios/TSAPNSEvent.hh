#import <Foundation/Foundation.h>
#import "PBObjcWrapper.hh"

@interface TSAPNSEvent : NSObject<PBObjcWrapper>

@property (nonatomic,strong) NSString* token;
@property (nonatomic,strong) NSString* appKey;
@property (nonatomic,strong) NSString* userId;

@property int op;
@property int typeId;

@property (readonly,nonatomic,strong) NSData *protocolData;

@end